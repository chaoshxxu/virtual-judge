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
import judge.tool.MultipleProxyHttpClientFactory;
import judge.tool.Tools;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class FZUSubmitter extends Submitter {

	static final String OJ_NAME = "FZU";
	static private boolean using[];
	static private String[] usernameList;
	static private String[] passwordList;
	static private HttpContext[] contexts;
	static private HttpClient client = MultipleProxyHttpClientFactory.getInstance(OJ_NAME);
	
	private HttpGet get;
	private HttpPost post;
	private HttpResponse response;
	private HttpEntity entity;
	private HttpHost host = new HttpHost("acm.fzu.edu.cn");
	private String html;

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
		languageList.put("0", "GNU C++");
		languageList.put("1", "GNU C");
		languageList.put("2", "Pascal");
		languageList.put("3", "Java");
		languageList.put("4", "Visual C++");
		languageList.put("5", "Visual C");
		sc.setAttribute("FZU", languageList);
	}

	private void getMaxRunId() throws ClientProtocolException, IOException {
		Pattern p = Pattern.compile("<tr onmouseover=\"hl\\(this\\);\" onmouseout=\"unhl\\(this\\);\" >\\s*<td>(\\d+)</td>");

		try {
			get = new HttpGet("/log.php");
			response = client.execute(host, get, contexts[idx]);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}

		Matcher m = p.matcher(html);
		if (m.find()) {
			maxRunId = Integer.parseInt(m.group(1));
			System.out.println("maxRunId : " + maxRunId);
		} else {
			throw new RuntimeException();
		}
	}
	
	private void login(String username, String password) throws ClientProtocolException, IOException {
		try {
			post = new HttpPost("/login.php?act=1");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("uname", username));
			nvps.add(new BasicNameValuePair("upassword", password));
			
			post.setEntity(new UrlEncodedFormEntity(nvps));
			
			response = client.execute(host, post, contexts[idx]);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		if (!html.contains("location.replace(\"index.php\")")) {
			throw new RuntimeException();
		}
	}

	private boolean isLoggedIn() throws ClientProtocolException, IOException {
		try {
			get = new HttpGet("/index.php");
			response = client.execute(host, get, contexts[idx]);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		if (html.contains(">Logout</a>")) {
			return true;
		} else {
			return false;
		}
	}

	private void submit() throws ClientProtocolException, IOException {
		Problem problem = (Problem) baseService.query(Problem.class, submission.getProblem().getId());
		
		try {
			post = new HttpPost("/submit.php?act=5");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("lang", submission.getLanguage()));
			nvps.add(new BasicNameValuePair("pid", problem.getOriginProb()));
			nvps.add(new BasicNameValuePair("code", submission.getSource()));
			
			post.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8")));
			
			response = client.execute(host, post, contexts[idx]);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
			
	        if (!html.contains("Your Program have been saved.")){
	        	throw new RuntimeException();
	        }
		} finally {
			EntityUtils.consume(entity);
		}
	}

	public void getResult(String username) throws Exception{
		String reg =
				"<td>(\\d+)</td>\\s*" +
				"<td>.*?</td>\\s*" +
				"<td>([\\s\\S]*?)</td>\\s*" +
				"<td>.*?</td>\\s*" +
				"<td>.*?</td>\\s*" +
				"<td>(.*?)</td>\\s*" +
				"<td>(.*?)</td>\\s*";
		Pattern p = Pattern.compile(reg);

		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			try {
				get = new HttpGet("/log.php?user=" + username);
				response = client.execute(host, get, contexts[idx]);
				entity = response.getEntity();
				html = EntityUtils.toString(entity);
			} finally {
				EntityUtils.consume(entity);
			}

			Matcher m = p.matcher(html);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId) {
				String result = m.group(2).replaceAll("<[\\s\\S]*?>", "").trim();
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
				if (!result.contains("ing")){
					if (result.equals("Accepted")){
						submission.setTime(Integer.parseInt(m.group(3).replaceAll("\\D+", "")));
						submission.setMemory(Integer.parseInt(m.group(4).replaceAll("\\D+", "")));
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
		try {
			get = new HttpGet("/ce.php?sid=" + runId);
			response = client.execute(host, get, contexts[idx]);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		submission.setAdditionalInfo("<pre>" + Tools.regFind(html, "Information:</b><br /><font color=\"blue\" size=\"-1\">([\\s\\S]*?)</font>").replaceAll("\n", "<br />") + "</pre>");
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
		int errorCode = 1;

		try {
			getMaxRunId();
			if (!isLoggedIn()) {
				login(usernameList[idx], passwordList[idx]);
			}
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
