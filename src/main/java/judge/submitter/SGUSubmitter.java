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
import judge.httpclient.MultipleProxyHttpClientFactory;
import judge.tool.ApplicationContainer;
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
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class SGUSubmitter extends Submitter {

	static final String OJ_NAME = "SGU";
	static private boolean using[];
	static private String[] usernameList;
	static private String[] passwordList;
	static private HttpContext[] contexts;
	static private HttpClient client = MultipleProxyHttpClientFactory.getInstance(OJ_NAME);
	
	private HttpGet get;
	private HttpPost post;
	private HttpResponse response;
	private HttpEntity entity;
	private HttpHost host = new HttpHost("acm.sgu.ru");
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
			contexts[i].setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
		}

		Map<String, String> languageList = new TreeMap<String, String>();
		languageList.put("GNU C (MinGW, GCC 4)", "GNU C (MinGW, GCC 4)");
		languageList.put("GNU CPP (MinGW, GCC 4)", "GNU CPP (MinGW, GCC 4)");
		languageList.put("Visual Studio C++ 2010", "Visual Studio C++ 2010");
		languageList.put("C#", "C#");
		languageList.put("Visual Studio C 2010", "Visual Studio C 2010");
		languageList.put("JAVA 7", "JAVA 7");
		languageList.put("Delphi 7.0", "Delphi 7.0");
		sc.setAttribute("SGU", languageList);
	}

	private void getMaxRunId() throws ClientProtocolException, IOException {
		Pattern p = Pattern.compile("<TD>(\\d{7,})</TD>");

		try {
			get = new HttpGet("/status.php");
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

	private void submit(String username, String password) throws Exception {
		Problem problem = (Problem) baseService.query(Problem.class, submission.getProblem().getId());
		
		try {
			post = new HttpPost("/sendfile.php?contest=0");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("elang", submission.getLanguage()));
			nvps.add(new BasicNameValuePair("id", username));
			nvps.add(new BasicNameValuePair("pass", password));
			nvps.add(new BasicNameValuePair("problem", problem.getOriginProb()));
			nvps.add(new BasicNameValuePair("source", submission.getSource()));
			
			post.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8")));
			
			response = client.execute(host, post, contexts[idx]);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
			
			if (html.contains("Your source seems to be dangerous")) {
				throw new Exception("judge_exception:Dangerous Code Error");
			}
			if (!html.contains("successfully submitted")){
				throw new Exception();
			}
		} finally {
			EntityUtils.consume(entity);
		}
	}

	public void getResult(String username) throws Exception{
		String reg = "<TD>(\\d{7,})</TD>[\\s\\S]*?<TD class=btab>([\\s\\S]*?)</TD>[\\s\\S]*?([\\d]*?) ms</TD><TD>([\\d]*?) kb</TD>";
		Pattern p = Pattern.compile(reg);

		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			try {
				get = new HttpGet("/status.php?idmode=1&id=" + username);
				response = client.execute(host, get, contexts[idx]);
				entity = response.getEntity();
				html = EntityUtils.toString(entity);
			} finally {
				EntityUtils.consume(entity);
			}

			Matcher m = p.matcher(html);
    		if (m.find() && Integer.parseInt(m.group(1)) > maxRunId){
    			String result = m.group(2).replaceAll("<[\\s\\S]*?>", "").trim();
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
    			if (!result.contains("ing")){
    				if (result.equals("Accepted")){
	    				submission.setMemory(Integer.parseInt(m.group(4)));
	    				submission.setTime(Integer.parseInt(m.group(3)));
    				} else if (result.contains("Compilation Error")) {
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
			get = new HttpGet("/cerror.php?id=" + runId);
			response = client.execute(host, get, contexts[idx]);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		submission.setAdditionalInfo(Tools.regFind(html, runId + "</TD><TD>(<pre>[\\s\\S]*?</pre>)"));
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
			submit(usernameList[idx], passwordList[idx]);
			errorCode = 2;
			submission.setStatus("Running & Judging");
			baseService.addOrModify(submission);
			getResult(usernameList[idx]);
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage() != null && e.getMessage().startsWith("judge_exception:")){
				submission.setStatus(e.getMessage().substring(16));
			} else {
				submission.setStatus("Judging Error " + errorCode);
			}
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
