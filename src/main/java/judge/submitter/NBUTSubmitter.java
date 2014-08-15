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
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleHttpResponse;
import judge.httpclient.SimpleHttpResponseMapper;
import judge.httpclient.SimpleHttpResponseValidator;
import judge.tool.ApplicationContainer;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public class NBUTSubmitter extends Submitter {

	static final String OJ_NAME = "NBUT";
	static private boolean using[];
	static private String[] usernameList;
	static private String[] passwordList;
	static private HttpContext[] contexts;
	
	private DedicatedHttpClient client;
	private HttpHost host = new HttpHost("cdn.ac.nbutoj.com");

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
			contexts[i].setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
		}

		Map<String, String> languageList = new TreeMap<String, String>();
		languageList.put("1", "GCC");
		languageList.put("2", "G++");
		languageList.put("4", "FPC");
		sc.setAttribute("NBUT", languageList);
	}

	private void getMaxRunId() throws ClientProtocolException, IOException {
		maxRunId = client.get("/Problem/status.xhtml", new SimpleHttpResponseMapper<Integer>() {
			@Override
			public Integer map(SimpleHttpResponse response) throws Exception {
				String html = response.getBody();
				Matcher matcher = Pattern.compile("<td style=\"text-align: center;\">(\\d+)").matcher(html);
				Validate.isTrue(matcher.find());
				return Integer.parseInt(matcher.group(1));
			}
		});
		System.out.println("maxRunId : " + maxRunId);
	}
	
	private void login(String username, String password) throws ClientProtocolException, IOException {
		String html = client.get("/User/login.xhtml?url=%2F").getBody();
		String ojVerify = Tools.regFind(html, "name=\"__OJVERIFY__\" value=\"(\\w+)\"");
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("__OJVERIFY__", ojVerify));
		nvps.add(new BasicNameValuePair("password", password));
		nvps.add(new BasicNameValuePair("username", username));
		System.out.println(username + " - " + password);
		HttpEntity entity = new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8"));
		
		client.post("/User/chklogin.xhtml", entity, new SimpleHttpResponseValidator() {
			@Override
			public void validate(SimpleHttpResponse response) throws Exception {
				Validate.isTrue(response.getBody().contains("1"));
			}
		});
	}

	private boolean isLoggedIn() throws ClientProtocolException, IOException {
		String html = client.get("/").getBody();
		return html.contains("title=\"登出\"");
	}

	private void submit() throws ClientProtocolException, IOException {
		Problem problem = (Problem) baseService.query(Problem.class, submission.getProblem().getId());
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("language", submission.getLanguage()));
		nvps.add(new BasicNameValuePair("code", submission.getSource()));
		nvps.add(new BasicNameValuePair("id", problem.getOriginProb()));
		HttpEntity entity = new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8"));

		client.post("/Problem/submitok.xhtml", entity, HttpStatusValidator.SC_OK);
	}

	public void getResult(String username) throws Exception{
		String reg =
				"<td style=\"text-align: center;\">(\\d+)</td>\\s*" +
				"<td style=\"text-align: center;\">[\\s\\S]*?</td>\\s*" +
				"<td style=\"text-align: center;\">[\\s\\S]*?</td>\\s*" +
				"<td style=\"text-align: center;\">([\\s\\S]*?)</td>\\s*" +
				"<td style=\"text-align: center;\">(\\d+)</td>\\s*" +
				"<td style=\"text-align: center;\">(\\d+)</td>";
		Pattern p = Pattern.compile(reg);
		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			String html = client.get("/Problem/status.xhtml?username=" + username).getBody();
			Matcher m = p.matcher(html);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId){
				String result = toCamel(m.group(2).replaceAll("<.*?>", "").trim());
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
				if (!result.contains("ing")){
					if (result.equals("Accepted")){
						submission.setTime(Integer.parseInt(m.group(3)));
						submission.setMemory(Integer.parseInt(m.group(4)));
					} else if (result.contains("Compilation error")) {
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
	
	private String toCamel(String string) {
		return (string.charAt(0) + string.substring(1).toLowerCase()).replace("_", " ");
	}

	private void getAdditionalInfo(String runId) throws HttpException, IOException {
		String html = client.get("/Problem/viewce.xhtml?submitid=" + runId).getBody();
		submission.setAdditionalInfo(Tools.regFind(html, "(<pre style=\"overflow-x: auto;\">[\\s\\S]*?</pre>)"));
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
		client = new DedicatedHttpClient(host, contexts[idx]);
		
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
		}
		synchronized (using) {
			using[idx] = false;
		}
	}
}
