package judge.submitter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.bean.Problem;
import judge.tool.ApplicationContainer;
import judge.tool.DedicatedHttpClient;
import judge.tool.MultipleProxyHttpClientFactory;
import judge.tool.SimpleHttpResponse;
import judge.tool.SimpleHttpResponseHandler;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public class POJSubmitter extends Submitter {

	static final String OJ_NAME = "POJ";
	static private boolean using[];
	static private String[] usernameList;
	static private String[] passwordList;
	static private HttpContext[] contexts;
	static private HttpClient deligateClient = MultipleProxyHttpClientFactory.getInstance(OJ_NAME);
	
	private DedicatedHttpClient client;
	private HttpHost host = new HttpHost("poj.org");

	static {
		List<String> uList = new ArrayList<String>(), pList = new ArrayList<String>();
		try {
			FileReader fr = new FileReader(ApplicationContainer.sc.getRealPath("WEB-INF/classes/accounts.conf"));
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String info[] = br.readLine().split("\\s+");
				if (info.length >= 3 && info[0].equalsIgnoreCase(OJ_NAME)){
					uList.add(info[1]);
					pList.add(info[2]);
				}
			}
			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		usernameList = uList.toArray(new String[0]);
		passwordList = pList.toArray(new String[0]);
		using = new boolean[usernameList.length];
		contexts = new HttpContext[usernameList.length];
		for (int i = 0; i < contexts.length; i++){
			CookieStore cookieStore = new BasicCookieStore();
			contexts[i] = new BasicHttpContext();
			contexts[i].setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		}

		Map<String, String> languageList = new TreeMap<String, String>();
		languageList.put("0", "G++");
		languageList.put("1", "GCC");
		languageList.put("2", "Java");
		languageList.put("3", "Pascal");
		languageList.put("4", "C++");
		languageList.put("5", "C");
		languageList.put("6", "Fortran");
		sc.setAttribute("POJ", languageList);
	}

	private void getMaxRunId() throws ClientProtocolException, IOException {
		client.get("/status", new SimpleHttpResponseHandler() {
			@Override
			public void handle(SimpleHttpResponse response) {
				String html = response.getBody();
				Matcher m = Pattern.compile("<tr align=center><td>(\\d+)").matcher(html);
				if (m.find()) {
					maxRunId = Integer.parseInt(m.group(1));
					System.out.println("maxRunId : " + maxRunId);
				} else {
					throw new RuntimeException();
				}
			}
		});
	}
	
	private void login(String username, String password) throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost("/login");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("B1", "login"));
		nvps.add(new BasicNameValuePair("password1", password));
		nvps.add(new BasicNameValuePair("url", "/"));
		nvps.add(new BasicNameValuePair("user_id1", username));
		post.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8")));
		
		client.execute(post, new SimpleHttpResponseHandler() {
			@Override
			public void handle(SimpleHttpResponse response) {
				Validate.isTrue(response.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY);
			}
		});
	}

	private boolean isLoggedIn() throws ClientProtocolException, IOException {
		String html = client.get("/").getBody();
		return html.contains(">Log Out</a>");
	}

	private void submit() throws ClientProtocolException, IOException {
		Problem problem = (Problem) baseService.query(Problem.class, submission.getProblem().getId());
		
		HttpPost post = new HttpPost("/submit");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("language", submission.getLanguage()));
		nvps.add(new BasicNameValuePair("problem_id", problem.getOriginProb()));
		nvps.add(new BasicNameValuePair("source", submission.getSource()));
		post.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8")));
		
		client.execute(post, new SimpleHttpResponseHandler() {
			@Override
			public void handle(SimpleHttpResponse response) {
				Validate.isTrue(response.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY);
			}
		});
	}

	public void getResult(String username) throws Exception{
		String reg = "<td>(\\d{7,})</td>[\\s\\S]*?<font[\\s\\S]*?>([\\s\\S]*?)</font>[\\s\\S]*?<td>([\\s\\S]*?)</td><td>([\\s\\S]*?)</td>";
		Pattern p = Pattern.compile(reg);
		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			String html = client.get("/status?user_id=" + username).getBody();
			Matcher m = p.matcher(html);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId) {
				String result = m.group(2).replaceAll("<[\\s\\S]*?>", "").trim();
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
				if (!result.contains("ing")){
					if (result.equals("Accepted")){
						submission.setMemory(Math.abs(Integer.parseInt(m.group(3).replaceAll("K", ""))));
						submission.setTime(Integer.parseInt(m.group(4).replaceAll("MS", "")));
					} else if (result.contains("Compile Error")) {
						getAdditionalInfo(submission.getRealRunId());
					}
					baseService.addOrModify(submission);
					return;
				}
				baseService.addOrModify(submission);
			}
			Thread.sleep(interval);
			interval += 500;
		}
		throw new Exception();
	}

	private void getAdditionalInfo(String runId) throws HttpException, IOException {
		String html = client.get("/showcompileinfo?solution_id=" + runId).getBody();
		submission.setAdditionalInfo(Tools.regFind(html, "(<pre>[\\s\\S]*?</pre>)"));
	}

	private int getIdleClient() {
		int length = usernameList.length;
		int begIdx = (int) (Math.random() * length);

		while(true) {
			synchronized (using) {
				for (int i = begIdx, j; i < begIdx + length; i++) {
					j = i % length;
					if (!using[j]) {
						using[j] = true;
						return j;
					}
				}
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void work() {
		idx = getIdleClient();
		client = new DedicatedHttpClient(host, contexts[idx], deligateClient);
		
		int errorCode = 1;

		try {
			if (!isLoggedIn()) {
				login(usernameList[idx], passwordList[idx]);
			}
			getMaxRunId();
			submit();
			errorCode = 2;
			submission.setStatus("Running & Judging");
			baseService.addOrModify(submission);
			getResult(usernameList[idx]);
		} catch (Exception e) {
			e.printStackTrace();
			submission.setStatus("Judging Error " + errorCode);
			baseService.addOrModify(submission);
		}
	}

	@Override
	public void waitForUnfreeze() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	//HDU限制每两次提交之间至少隔3秒
		synchronized (using) {
			using[idx] = false;
		}
	}
}
