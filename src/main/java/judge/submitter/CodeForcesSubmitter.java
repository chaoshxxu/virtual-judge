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

import judge.tool.ApplicationContainer;
import judge.tool.Tools;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

public class CodeForcesSubmitter extends Submitter {

	static final String OJ_NAME = "CodeForces";
	static private DefaultHttpClient clientList[];
	static private boolean using[];
	static private String[] usernameList;
	static private String[] passwordList;

	private DefaultHttpClient client;
	private HttpGet get;
	private HttpPost post;
	private HttpResponse response;
	private HttpEntity entity;
	private HttpHost host = new HttpHost("codeforces.com");
	private String html;
	
	private String tta;
	private String csrfToken;

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
		languageList.put("1", "GNU C++ 4.6");
		languageList.put("2", "Microsoft Visual C++ 2005+");
		languageList.put("3", "Delphi 7");
		languageList.put("4", "Free Pascal 2");
		languageList.put("5", "Java 6");
		languageList.put("6", "PHP 5.2+");
		languageList.put("7", "Python 2.6+");
		languageList.put("8", "Ruby 1.7+");
		languageList.put("9", "C# Mono 2.6+");
		languageList.put("10", "GNU C 4");
		languageList.put("12", "Haskell GHC 6.12");
		languageList.put("13", "Perl 5.12+");
		languageList.put("14", "ActiveTcl 8.5");
		languageList.put("15", "Io-2008-01-07 (Win32)");
		languageList.put("16", "GNU C++0x 4");
		languageList.put("17", "Pike 7.8");
		languageList.put("19", "OCaml 3.12");
		languageList.put("20", "Scala 2.9");
		languageList.put("23", "Java 7");
		languageList.put("28", "D DMD32 Compiler v2");
		sc.setAttribute("CodeForces", languageList);
	}

	private void getMaxRunId() throws ClientProtocolException, IOException {
		Pattern p = Pattern.compile("submissionId=\"(\\d+)\"");

		try {
			get = new HttpGet("/problemset/status");
			response = client.execute(host, get);
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
	
	private void login(String username, String password) throws ClientProtocolException, IOException {
		getTokens();
		try {
			post = new HttpPost("/enter");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("csrf_token", csrfToken));
			nvps.add(new BasicNameValuePair("_tta", tta));
			nvps.add(new BasicNameValuePair("action", "enter"));
			nvps.add(new BasicNameValuePair("handle", username));
			nvps.add(new BasicNameValuePair("password", password));
			nvps.add(new BasicNameValuePair("remember", "on"));

			post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
			
			response = client.execute(host, post);
			entity = response.getEntity();
			
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
				throw new RuntimeException();
			}
		} finally {
			EntityUtils.consume(entity);
		}
	}
	
	/**
	 * Currently, get the following tokens: _tta, csrf_token
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	private void getTokens() throws ParseException, IOException {
//		if (csrfToken != null && tta != null) {
//			return;
//		}
		
		try {
			get = new HttpGet("/");
			response = client.execute(host, get);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		
		csrfToken = Tools.regFind(html, "data-csrf='(\\w+)'");
		
		if (tta == null) {
			String _39ce7 = null;
			for (Cookie cookie : client.getCookieStore().getCookies()) {
				if (cookie.getName().equals("39ce7")) {
					_39ce7 = cookie.getValue();
				}
			}
			if (_39ce7 == null) {
				throw new RuntimeException();
			}
	
			Integer _tta = 0;
			for (int c = 0; c < _39ce7.length(); c++) {
				_tta = (_tta + (c + 1) * (c + 2) * _39ce7.charAt(c)) % 1009;
				if (c % 3 == 0)
					_tta++;
				if (c % 2 == 0)
					_tta *= 2;
				if (c > 0)
					_tta -= ((int) (_39ce7.charAt(c / 2) / 2)) * (_tta % 5);
				while (_tta < 0)
					_tta += 1009;
				while (_tta >= 1009)
					_tta -= 1009;
			}
			tta = _tta.toString();
		}

	}

	private boolean isLoggedIn() throws ClientProtocolException, IOException {
		try {
			get = new HttpGet("/");
			response = client.execute(host, get);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		return html.contains("/logout\">");
	}

	private void submit() throws Exception{
		String source = submission.getSource() + "\n";
		int random = (int) (Math.random() * 87654321);
		while (random > 0) {
			source += random % 2 == 0 ? ' ' : '\t';
			random /= 2;
		}
		getTokens();
		post = new HttpPost("/problemset/submit?csrf_token=" + csrfToken);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("csrf_token", csrfToken));
		nvps.add(new BasicNameValuePair("_tta", tta));
		nvps.add(new BasicNameValuePair("action", "submitSolutionFormSubmitted"));
		nvps.add(new BasicNameValuePair("submittedProblemCode", submission.getOriginProb()));
		nvps.add(new BasicNameValuePair("programTypeId", submission.getLanguage()));
		nvps.add(new BasicNameValuePair("source", source));
		nvps.add(new BasicNameValuePair("sourceFile", ""));
		nvps.add(new BasicNameValuePair("sourceCodeConfirmed", "true"));
		nvps.add(new BasicNameValuePair("doNotShowWarningAgain", "on"));
		post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

		try {
			response = client.execute(host, post);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);

			if (html.contains("error for__programTypeId")) {
				throw new RuntimeException("judge_exception:Language Error");
			}
			if (html.contains("error for__source")) {
				throw new Exception("judge_exception:Source Code Error");
			}
			
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_MOVED_TEMPORARILY){
				throw new Exception();
			}
			if (!response.getFirstHeader("Location").getValue().contains("status")) {
				throw new Exception();
			}
		} finally {
			EntityUtils.consume(entity);
		}

	}

	public void getResult(String username) throws Exception{
		Pattern p = Pattern.compile(username + "</a>\\s*</td>[\\s\\S]*?submissionId=\"(\\d+)\" >([\\s\\S]*?)</td>[\\s\\S]*?(\\d+)[\\s\\S]*?(\\d+)");

		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			try {
				get = new HttpGet("/problemset/status");
				response = client.execute(host, get);
				entity = response.getEntity();
				html = EntityUtils.toString(entity);
			} finally {
				EntityUtils.consume(entity);
			}

			Matcher m = p.matcher(html);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId) {
				String result = m.group(2).replaceAll("<[\\s\\S]*?>", "").trim().replaceAll("judge\\b", "judging").replaceAll("queue", "queueing");
				if (result.isEmpty()) {
					result = "processing";
				}
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
				if (!result.contains("ing")){
					if (result.equals("Accepted")){
						submission.setTime(Integer.parseInt(m.group(3)));
						submission.setMemory(Integer.parseInt(m.group(4)));
					} else if (result.contains("Compilation")) {
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
		post = new HttpPost("/data/judgeProtocol");

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("submissionId", runId));
		nvps.add(new BasicNameValuePair("csrf_token", csrfToken));
		post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

		try {
			response = client.execute(host, post);
			entity = response.getEntity();
			String additionalInfo = EntityUtils.toString(entity);
			submission.setAdditionalInfo("<pre>" + additionalInfo.replaceAll("(\\\\r)?\\\\n", "\n").replaceAll("\\\\\\\\", "\\\\") + "</pre>");
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
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		synchronized (using) {
			using[idx] = false;
		}
	}
}
