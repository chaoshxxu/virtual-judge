package judge.submitter;

import java.io.BufferedReader;
import java.io.File;
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

	private DefaultHttpClient httpClient;
	private HttpEntity entity;

	private String tta;
	private String xsrf;

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

	private void getMaxRunId() throws Exception {
		try {
			HttpGet req = new HttpGet("http://codeforces.com/problemset/status");
			HttpResponse rsp = httpClient.execute(req);
			entity = rsp.getEntity();

			Matcher m = Pattern.compile("submissionId=\"(\\d+)\"").matcher(EntityUtils.toString(entity));

			if (m.find()) {
				maxRunId = Integer.parseInt(m.group(1));
				System.out.println("maxRunId : " + maxRunId);
			} else {
				throw new Exception();
			}
		} finally {
			EntityUtils.consume(entity);
		}
	}

	private void submit() throws Exception{
		try {
			HttpGet get = new HttpGet("http://codeforces.com/");
			HttpResponse rsp = httpClient.execute(get);
			entity = rsp.getEntity();

			if (!EntityUtils.toString(entity).contains("/logout\">")) {
				throw new Exception();
			}
		} finally {
			EntityUtils.consume(entity);
		}

		String source = submission.getSource() + "\n";
		int random = (int) (Math.random() * 87654321);
		while (random > 0) {
			source += random % 2 == 0 ? ' ' : '\t';
			random /= 2;
		}
		String contestId = submission.getOriginProb().substring(0, submission.getOriginProb().length() - 1);
		String problemNum = submission.getOriginProb().substring(submission.getOriginProb().length() - 1);

		HttpPost httpPost = new HttpPost("http://codeforces.com/problemset/submit");

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("xsrf", getXsrf()));
		nvps.add(new BasicNameValuePair("_tta", getTTA()));
		nvps.add(new BasicNameValuePair("action", "submitSolutionFormSubmitted"));
		nvps.add(new BasicNameValuePair("submittedProblemCode", contestId + problemNum));
		nvps.add(new BasicNameValuePair("programTypeId", submission.getLanguage()));
		nvps.add(new BasicNameValuePair("source", source));
		nvps.add(new BasicNameValuePair("sourceFile", ""));
		nvps.add(new BasicNameValuePair("sourceCodeConfirmed", "true"));
		nvps.add(new BasicNameValuePair("doNotShowWarningAgain", "on"));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

		System.out.println("submit...");

		try {
			HttpResponse response = httpClient.execute(httpPost);
			entity = response.getEntity();

			String tLine = EntityUtils.toString(entity);
			if (tLine.contains("error for__programTypeId")) {
				throw new Exception("judge_exception:Language Error");
			}
			if (tLine.contains("error for__source")) {
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

	private void login(String username, String password) throws Exception{
		try {
			HttpPost httpost = new HttpPost("http://codeforces.com/enter");

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("xsrf", getXsrf()));
			nvps.add(new BasicNameValuePair("_tta", getTTA()));
			nvps.add(new BasicNameValuePair("action", "enter"));
			nvps.add(new BasicNameValuePair("handle", username));
			nvps.add(new BasicNameValuePair("password", password));
			nvps.add(new BasicNameValuePair("remember", "on"));

			httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

			HttpResponse response = httpClient.execute(httpost);
			entity = response.getEntity();
		} finally {
			EntityUtils.consume(entity);
		}
	}

	private String getXsrf() throws ParseException, IOException {
		if (xsrf != null) {
			return xsrf;
		}
		String html = null;
		try {
			HttpGet req = new HttpGet("http://codeforces.com");
			HttpResponse rsp = httpClient.execute(req);
			entity = rsp.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		xsrf = Tools.regFind(html, "data-xsrf='(\\w+)'");
		return xsrf;
	}


	private String getTTA() throws HttpException, IOException {
		if (tta != null) {
			return tta;
		}

		String _39ce7 = null;
		for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
			if (cookie.getName().equals("39ce7")) {
				_39ce7 = cookie.getValue();
			}
		}
		if (_39ce7 == null) {
			HttpGet req = new HttpGet("http://codeforces.com");
			HttpResponse rsp = httpClient.execute(req);
			HttpEntity entity = rsp.getEntity();
			for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
				if (cookie.getName().equals("39ce7")) {
					_39ce7 = cookie.getValue();
				}
			}
			EntityUtils.consume(entity);
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
		return tta;
	}

	public void getResult(String username) throws Exception{
		String reg = username + "</a>    </td>[\\s\\S]*?submissionId=\"(\\d+)\" >([\\s\\S]*?)</td>[\\s\\S]*?(\\d+)[\\s\\S]*?(\\d+)", result;
		Pattern p = Pattern.compile(reg);

		HttpGet get = new HttpGet("http://codeforces.com/problemset/status");
		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			String tLine = null;
			try {
				HttpResponse rsp = httpClient.execute(get);
				entity = rsp.getEntity();
				tLine = EntityUtils.toString(entity);
			} finally {
				EntityUtils.consume(entity);
			}

			Matcher m = p.matcher(tLine);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId) {
				result = m.group(2).replaceAll("<[\\s\\S]*?>", "").trim().replaceAll("judge\\b", "judging").replaceAll("queue", "queueing");
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
		HttpPost post = new HttpPost("http://codeforces.com/data/judgeProtocol");

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("submissionId", runId));
		post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

		try {
			HttpResponse rsp = httpClient.execute(post);
			entity = rsp.getEntity();
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
						httpClient = clientList[j];
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
			try {
				//第一次尝试提交
				submit();
			} catch (Exception e1) {
				//失败,认为是未登录所致
				e1.printStackTrace();
				Thread.sleep(2000);
				login(usernameList[idx], passwordList[idx]);
				Thread.sleep(2000);
				submit();
			}
			errorCode = 2;
			submission.setStatus("Running & Judging");
			baseService.addOrModify(submission);
			Thread.sleep(2000);
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
		}	//CodeForces貌似不限制每两次提交之间的提交间隔
		synchronized (using) {
			using[idx] = false;
		}
	}

}
