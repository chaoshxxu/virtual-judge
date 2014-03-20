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

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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

public class URALSubmitter extends Submitter {

	static final String OJ_NAME = "URAL";
	static private boolean using[];
	static private String[] usernameList;
	static private String[] passwordList;
	static private HttpContext[] contexts;
	static private HttpClient client = MultipleProxyHttpClientFactory.getInstance(OJ_NAME);
	
	private HttpGet get;
	private HttpPost post;
	private HttpResponse response;
	private HttpEntity entity;
	private HttpHost host = new HttpHost("acm.timus.ru");
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
		languageList.put("3", "FreePascal 2.0.4");
		languageList.put("9", "Visual C 2010");
		languageList.put("10", "Visual C++ 2010");
		languageList.put("11", "Visual C# 2010");
		languageList.put("12", "Java 1.7");
		languageList.put("14", "Go 1.0.3");
		languageList.put("15", "VB.NET 2010");
		languageList.put("16", "Python 2.7");
		languageList.put("17", "Python 3.3");
		languageList.put("18", "Ruby 1.9.3");
		languageList.put("19", "Haskell 7.6.1");
		languageList.put("20", "GCC 4.7.2");
		languageList.put("21", "G++ 4.7.2");
		languageList.put("22", "GCC 4.7.2 C11");
		languageList.put("23", "G++ 4.7.2 C++11");
		sc.setAttribute("URAL", languageList);
	}

	private void getMaxRunId() throws ClientProtocolException, IOException {
		Pattern p = Pattern.compile("<TD class=\"id\">(\\d+)");

		try {
			get = new HttpGet("/status.aspx");
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

	private void submit(String password) throws Exception {
		Problem problem = (Problem) baseService.query(Problem.class, submission.getProblem().getId());
		
		try {
			post = new HttpPost("/submit.aspx");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("Action", "submit"));
			nvps.add(new BasicNameValuePair("Language", submission.getLanguage()));
			nvps.add(new BasicNameValuePair("ProblemNum", problem.getOriginProb()));
			nvps.add(new BasicNameValuePair("Source", submission.getSource()));
			nvps.add(new BasicNameValuePair("JudgeID", password));
			nvps.add(new BasicNameValuePair("SpaceID", "1"));
			
			post.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8")));
			
			response = client.execute(host, post, contexts[idx]);
			entity = response.getEntity();
			
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
				throw new RuntimeException();
			}
		} finally {
			EntityUtils.consume(entity);
		}
	}

	public void getResult(String username) throws Exception{
		String reg = "aspx/(\\d+)[\\s\\S]*?class=\"verdict_\\w{2,5}\">([\\s\\S]*?)</TD>[\\s\\S]*?runtime\">([\\d\\.]*)[\\s\\S]*?memory\">([\\d\\s]*)";
		Pattern p = Pattern.compile(reg);

		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			try {
				get = new HttpGet("/status.aspx?author=" + username.replaceAll("\\D", ""));
				response = client.execute(host, get, contexts[idx]);
				entity = response.getEntity();
				html = EntityUtils.toString(entity);
			} finally {
				EntityUtils.consume(entity);
			}

			Matcher m = p.matcher(html);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId){
				String result = m.group(2).replaceAll("<[\\s\\S]*?>", "").trim().replace("floating-point", "float-point");
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
				if (!result.contains("ing")){
					if (result.equals("Accepted")){
						submission.setMemory(Integer.parseInt(m.group(4).replaceAll(" ", "")));
						submission.setTime((int)(0.5 + 1000 * Double.parseDouble(m.group(3))));
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

	private void getAdditionalInfo(String runId) throws HttpException, IOException {
		try {
			get = new HttpGet("/ce.aspx?id=" + runId);
			response = client.execute(host, get, contexts[idx]);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		submission.setAdditionalInfo("<pre>" + html + "</pre>");
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
			submit(passwordList[idx]);
			errorCode = 2;
			submission.setStatus("Running & Judging");
			baseService.addOrModify(submission);
			getResult(passwordList[idx]);
		} catch (Exception e) {
			e.printStackTrace();
			submission.setStatus("Judging Error " + errorCode);
			baseService.addOrModify(submission);
		}
	}

	@Override
	public void waitForUnfreeze() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		synchronized (using) {
			using[idx] = false;
		}
	}
}
