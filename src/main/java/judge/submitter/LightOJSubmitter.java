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
import judge.tool.Tools;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

public class LightOJSubmitter extends Submitter {

	static final String OJ_NAME = "LightOJ";
	static private DefaultHttpClient clientList[];
	static private boolean using[];
	static public String[] usernameList;
	static public String[] passwordList;

	private DefaultHttpClient client;
	private HttpGet get;
	private HttpPost post;
	private HttpResponse response;
	private HttpEntity entity;
	private HttpHost host = new HttpHost("www.lightoj.com");
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
		clientList = new DefaultHttpClient[usernameList.length];
		HttpHost proxy = new HttpHost("127.0.0.1", 8087);
		for (int i = 0; i < clientList.length; i++){
			clientList[i] = new DefaultHttpClient();
			clientList[i].getParams().setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.83 Safari/537.1");
			clientList[i].getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
			clientList[i].getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
			clientList[i].getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}

		Map<String, String> languageList = new TreeMap<String, String>();
		languageList.put("C", "C");
		languageList.put("C++", "C++");
		languageList.put("JAVA", "JAVA");
		languageList.put("PASCAL", "PASCAL");
		languageList.put("PYTHON", "PYTHON");
		sc.setAttribute("LightOJ", languageList);
	}

	private void getMaxRunId() throws ClientProtocolException, IOException {
		Pattern p = Pattern.compile("<th class=\"newone\"\\s*>(\\d+)");

		try {
			get = new HttpGet("/volume_submissions.php");
			response = client.execute(host, get);
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
			post = new HttpPost("/login_check.php");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("mypassword", password));
			nvps.add(new BasicNameValuePair("myrem", "1"));
			nvps.add(new BasicNameValuePair("myuserid", username));
			
			post.setEntity(new UrlEncodedFormEntity(nvps));
			
			response = client.execute(host, post);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
	}

	private boolean isLoggedIn() throws ClientProtocolException, IOException {
		try {
			get = new HttpGet("/index.php");
			response = client.execute(host, get);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		if (html.contains("href=\"login_logout.php\"")) {
			return true;
		} else {
			return false;
		}
	}

	private void submit() throws ClientProtocolException, IOException {
		Problem problem = (Problem) baseService.query(Problem.class, submission.getProblem().getId());
		
		try {
			post = new HttpPost("/volume_submit.php");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("language", submission.getLanguage()));
			nvps.add(new BasicNameValuePair("sub_problem", problem.getOriginProb()));
			nvps.add(new BasicNameValuePair("code", submission.getSource()));
			
			post.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8")));
			
			response = client.execute(host, post);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
			
	        if (!html.contains("location.href='volume_usersubmissions.php'")){
	        	throw new RuntimeException();
	        }
		} finally {
			EntityUtils.consume(entity);
		}
	}

	public void getResult(String username) throws Exception{
		String reg = "newone[\\s\\S]*?sub_id=(\\d+)[\\s\\S]*?<td class=\"newone\">[\\s\\S]*?<td class=\"newone\">[\\s\\S]*?<td class=\"newone\">\\s*([-\\d\\.]+)[\\s\\S]*?<td class=\"newone\">\\s*([-\\d\\.]+)[\\s\\S]*?<td class=\"newone\">([\\s\\S]*?)</td>";
		Pattern p = Pattern.compile(reg);

		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			try {
				get = new HttpGet("/volume_usersubmissions.php");
				response = client.execute(host, get);
				entity = response.getEntity();
				html = EntityUtils.toString(entity);
			} finally {
				EntityUtils.consume(entity);
			}

			Matcher m = p.matcher(html);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId) {
				html = m.group(4).replaceAll("<[\\s\\S]*?>", "").trim();
				if ("Not Judged Yet".equals(html)) {
					html = "Judging";
				}
				submission.setStatus(html);
				submission.setRealRunId(m.group(1));
    			if (!html.contains("ing")){
    				if (html.equals("Accepted")){
	    				submission.setTime((int)(1000 * Double.parseDouble(m.group(2))));
	    				submission.setMemory(Integer.parseInt(m.group(3)));
    				} else if (html.contains("Compilation")) {
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
			get = new HttpGet("/volume_showcode.php?sub_id=" + runId);
			response = client.execute(host, get);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		submission.setAdditionalInfo("<pre>" + Tools.regFind(html, "<textarea style=[^>]+>([\\s\\S]*?)</textarea>") + "</pre>");
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
						client = clientList[j];
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
