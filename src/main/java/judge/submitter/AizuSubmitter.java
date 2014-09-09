package judge.submitter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.MultipleProxyHttpClientFactory;
import judge.tool.ApplicationContainer;
import judge.tool.SpringBean;
import judge.tool.Tools;

import org.apache.http.Consts;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AizuSubmitter extends Submitter {
	private final static Logger log = LoggerFactory.getLogger(AizuSubmitter.class);

	static final String OJ_NAME = "Aizu";
	static private boolean using[];
	static private String[] usernameList;
	static private String[] passwordList;
	static private HttpContext[] contexts;
	static private HttpClient client = SpringBean.getBean(MultipleProxyHttpClientFactory.class).getInstance(OJ_NAME);
	
	private HttpGet get;
	private HttpPost post;
	private HttpResponse response;
	private HttpEntity entity;
	private HttpHost host = new HttpHost("judge.u-aizu.ac.jp");
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
		languageList.put("C++", "C++");
		languageList.put("C", "C");
		languageList.put("JAVA", "JAVA");
		sc.setAttribute("Aizu", languageList);
	}

	private void getMaxRunId() throws ClientProtocolException, IOException {
		Pattern p = Pattern.compile("show_code\\.jsp\\?runID=(\\d{6,})");

		try {
			get = new HttpGet("/onlinejudge/status.jsp");
			response = client.execute(host, get, contexts[idx]);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}

		Matcher m = p.matcher(html);
		if (m.find()) {
			maxRunId = Integer.parseInt(m.group(1));
		} else {
			throw new RuntimeException();
		}
	}

	private void submit(String username, String password) throws Exception{
		post = new HttpPost("/onlinejudge/servlet/Submit");

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("language", submission.getLanguage()));
		nvps.add(new BasicNameValuePair("password", password));
		nvps.add(new BasicNameValuePair("problemNO", submission.getOriginProb()));
		nvps.add(new BasicNameValuePair("sourceCode", submission.getSource()));
		nvps.add(new BasicNameValuePair("userID", username));
		post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

		try {
			response = client.execute(host, post, contexts[idx]);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);

			if (!html.contains("HTTP-EQUIV=\"refresh\"")){
				throw new Exception();
			}
		} finally {
			EntityUtils.consume(entity);
		}
	}

	public void getResult(String username) throws Exception{
		String reg =
				"<td.*?#(\\d{6,})[\\s\\S]*?" +
				"<td[\\s\\S]*?" +
				"<td.*?" + username + "[\\s\\S]*?" +
				"<td[\\s\\S]*?" +
				"<td[\\s\\S]*?" +
				"<td.*?icon\\w+\">:([\\s\\S]*?)</span>[\\s\\S]*?" +
				"<td.*?>.*?</td>[\\s\\S]*?" +
				"<td.*?>.*?</td>[\\s\\S]*?" +
				"<td.*?>.*?</td>[\\s\\S]*?" +
				"<td.*?>(.*?)</td>[\\s\\S]*?" +
				"<td.*?>(.*?)</td>[\\s\\S]*?" +
				"<td.*?>.*?</td>[\\s\\S]*?" +
				"<td.*?>.*?</td>[\\s\\S]*?";
		Pattern p = Pattern.compile(reg);

		long cur = new Date().getTime();
		long interval = 2000;
		while (new Date().getTime() - cur < 600000){
			try {
				get = new HttpGet("/onlinejudge/status.jsp");
				response = client.execute(host, get, contexts[idx]);
				entity = response.getEntity();
				html = EntityUtils.toString(entity);
			} finally {
				EntityUtils.consume(entity);
			}

			Matcher m = p.matcher(html);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId){
				String result = m.group(2).replaceAll("<[^<>]*>", "").trim();
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
				if (!result.contains("ing")){
					if (result.equals("Accepted")){
						submission.setTime(calcTime(m.group(3)));
						submission.setMemory(calcMemory(m.group(4)));
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
	
	private int calcTime(String s) {
		log.info(s);
		Matcher matcher = Pattern.compile("(\\d+):(\\d+)").matcher(s);
		if (matcher.find()) {
			Integer a = Integer.parseInt(matcher.group(1), 10);
			Integer b = Integer.parseInt(matcher.group(2), 10);
			return a * 1000 + b * 10;
		} else {
			return 0;
		}
	}

	private int calcMemory(String s) {
		String memory = Tools.regFind(s, "(\\d+)");
		return memory.isEmpty() ? 0 : Integer.parseInt(memory);
	}

	private void getAdditionalInfo(String runId) throws HttpException, IOException {
		get = new HttpGet("/onlinejudge/compile_log.jsp?runID=" + runId);

		try {
			response = client.execute(host, get, contexts[idx]);
			entity = response.getEntity();
			String html = EntityUtils.toString(entity);
			String additionalInfo = html.substring(5 + html.indexOf("</h3>"));
			submission.setAdditionalInfo(additionalInfo);
		} finally {
			EntityUtils.consume(entity);
		}
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
			submission.setStatus("Judging Error " + errorCode);
			baseService.addOrModify(submission);
		}
	}

	@Override
	public void waitForUnfreeze() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		synchronized (using) {
			using[idx] = false;
		}
	}
}
