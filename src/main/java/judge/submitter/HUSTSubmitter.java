package judge.submitter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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
import judge.tool.SpringBean;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
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
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HUSTSubmitter extends Submitter {
	private final static Logger log = LoggerFactory.getLogger(HUSTSubmitter.class);

	static final String OJ_NAME = "HUST";
	static private boolean using[];
	static private String[] usernameList;
	static private String[] passwordList;
	static private HttpContext[] contexts;
	static private HttpClient client = SpringBean.getBean(MultipleProxyHttpClientFactory.class).getInstance(OJ_NAME);
	
	static public String[] captachaStrings;
	static public File[] captchaImages;
	
	private HttpGet get;
	private HttpPost post;
	private HttpResponse response;
	private HttpEntity entity;
	private HttpHost host = new HttpHost("acm.hust.edu.cn");
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
		captachaStrings = new String[usernameList.length];
		captchaImages = new File[usernameList.length];
		for (int i = 0; i < contexts.length; i++){
			CookieStore cookieStore = new BasicCookieStore();
			contexts[i] = new BasicHttpContext();
			contexts[i].setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
		}

		Map<String, String> languageList = new TreeMap<String, String>();
		languageList.put("0", "C");
		languageList.put("1", "C++");
		languageList.put("2", "Pascal");
        languageList.put("3", "Java");
		sc.setAttribute("HUST", languageList);
	}

	private void getMaxRunId(String username) throws ClientProtocolException, IOException {
		String url = "/status";
		if (!StringUtils.isEmpty(username)) {
			url += "?uid=" + username;
		}
		
		Pattern p1 = Pattern.compile("/solution/source/(\\d+)");
		Pattern p2 = Pattern.compile("<td>\\s{5,}(\\d+)\\s{5,}</td>");

		try {
			get = new HttpGet(url);
			response = client.execute(host, get, contexts[idx]);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		
		Matcher m1 = p1.matcher(html);
		Matcher m2 = p2.matcher(html);
		if (m1.find()) {
			maxRunId = Integer.parseInt(m1.group(1));
		} else if (m2.find()) {
			maxRunId = Integer.parseInt(m2.group(1));
		} else {
			throw new RuntimeException();
		}
		log.info("maxRunId : " + maxRunId);
	}
	
	private void login(String username, String password) throws ClientProtocolException, IOException {
		try {
			post = new HttpPost("/user/login");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("username", username));
			nvps.add(new BasicNameValuePair("pwd", password));
			nvps.add(new BasicNameValuePair("code", captachaStrings[idx]));
			
			post.setEntity(new UrlEncodedFormEntity(nvps));
			response = client.execute(host, post, contexts[idx]);

			Validate.isTrue(response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY);
		} finally {
			EntityUtils.consume(response.getEntity());
		}
	}

	private boolean isLoggedIn() throws ClientProtocolException, IOException {
		try {
			get = new HttpGet("/");
			response = client.execute(host, get, contexts[idx]);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		return html.contains("/logout"); 
	}

	private void submit() throws ClientProtocolException, IOException {
		Problem problem = (Problem) baseService.query(Problem.class, submission.getProblem().getId());
		
		try {
			post = new HttpPost("/problem/submit");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("language", submission.getLanguage()));
			nvps.add(new BasicNameValuePair("pid", problem.getOriginProb()));
			nvps.add(new BasicNameValuePair("source", submission.getSource()));
			
			post.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8")));
			
			response = client.execute(host, post, contexts[idx]);
			entity = response.getEntity();
			
			Validate.isTrue(response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY);
		} finally {
			EntityUtils.consume(entity);
		}
	}

	public void getResult(String username) throws Exception {
		getMaxRunId(username);
		
		String reg = 
		        "<span class=\"badge\">(.*?)</span>\\s*Result[\\s\\S]*?" +
		        "<span class=\"badge\">(\\d+)ms</span>\\s*Time[\\s\\S]*?" +
		        "<span class=\"badge\">(\\d+)kb</span>\\s*Memory";
		Pattern p = Pattern.compile(reg);

		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			try {
				get = new HttpGet("/solution/source/" + maxRunId);
				response = client.execute(host, get, contexts[idx]);
				entity = response.getEntity();
				html = EntityUtils.toString(entity);
			} finally {
				EntityUtils.consume(entity);
			}

			Matcher m = p.matcher(html);
			if (m.find()) {
				String result = m.group(1).trim();
				submission.setStatus(result);
				submission.setRealRunId(maxRunId + "");
				if (!result.contains("ing")){
    				if (result.equals("Accepted")){
    					submission.setTime(Integer.parseInt(m.group(2)));
	    				submission.setMemory(Integer.parseInt(m.group(3)));
    				} else if (result.contains("Compile Error")) {
    					submission.setAdditionalInfo(Tools.regFind(html, "(<pre class=\"col-sm-12 linenums\">[\\s\\S]*?</pre>)"));
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

	private void downloadCaptcha() throws ClientProtocolException, IOException {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		String filePath = "hust_captcha_" + idx;
		captchaImages[idx] = new File(filePath);
		try {
			get = new HttpGet("/index/captcha");
			response = client.execute(host, get, contexts[idx]);
			entity = response.getEntity();
			bis = new BufferedInputStream(entity.getContent());
			bos = new BufferedOutputStream(new FileOutputStream(captchaImages[idx]));
			int inByte;
			while((inByte = bis.read()) != -1) {
				bos.write(inByte);
			}
		} finally {
			bis.close();
			bos.close();
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
			getMaxRunId(null);
			if (!isLoggedIn()) {
				try {
					login(usernameList[idx], passwordList[idx]);
				} catch (Exception e) {
					downloadCaptcha();
					throw new RuntimeException(e);
				}
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
