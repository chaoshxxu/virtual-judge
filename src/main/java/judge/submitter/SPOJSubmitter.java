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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

public class SPOJSubmitter extends Submitter {

	static final String OJ_NAME = "SPOJ";
	static private DefaultHttpClient clientList[];
	static private boolean using[];
	static private String[] usernameList;
	static private String[] passwordList;

	private DefaultHttpClient client;
	private HttpGet get;
	private HttpPost post;
	private HttpResponse response;
	private HttpEntity entity;
	private HttpHost host = new HttpHost("www.spoj.com", 80);
	private String html;

	private String runId;

	static {
		List<String> uList = new ArrayList<String>(), pList = new ArrayList<String>();
		try {
			FileReader fr = new FileReader(ApplicationContainer.sc.getRealPath("WEB-INF/classes/accounts.conf"));
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String info[] = br.readLine().split("\\s+");
				if (info.length >= 3 && info[0].equalsIgnoreCase(OJ_NAME)) {
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
		for (int i = 0; i < clientList.length; i++) {
			clientList[i] = new DefaultHttpClient();
			clientList[i].getParams().setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36");
			clientList[i].getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
			clientList[i].getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
		}


		Map<String, String> languageList = new TreeMap<String, String>();
		languageList.put("7", "ADA 95 (gnat 4.3.2)");
		languageList.put("13", "Assembler (nasm 2.03.01)");
		languageList.put("104", "Awk (gawk-3.1.6)");
		languageList.put("28", "Bash (bash-4.0.37)");
		languageList.put("12", "Brainf**k (bff 1.0.3.1)");
		languageList.put("11", "C (gcc 4.3.2)");
		languageList.put("27", "C# (gmcs 2.0.1)");
		languageList.put("41", "C++ (g++ 4.3.2)");
		languageList.put("1", "C++ (g++ 4.0.0-8)");
		languageList.put("34", "C99 strict (gcc 4.3.2)");
		languageList.put("14", "Clips (clips 6.24)");
		languageList.put("111", "Clojure (clojure 1.1.0)");
		languageList.put("31", "Common Lisp (sbcl 1.0.18)");
		languageList.put("32", "Common Lisp (clisp 2.44.1)");
		languageList.put("20", "D (gdc 4.1.3)");
		languageList.put("36", "Erlang (erl 5.6.3)");
		languageList.put("124", "F# (fsharp 2.0.0)");
		languageList.put("5", "Fortran 95 (gfortran 4.3.2)");
		languageList.put("114", "Go (gc 2010-07-14)");
		languageList.put("21", "Haskell (ghc 6.10.4)");
		languageList.put("16", "Icon (iconc 9.4.3)");
		languageList.put("9", "Intercal (ick 0.28-4)");
		languageList.put("24", "JAR (JavaSE 6)");
		languageList.put("10", "Java (JavaSE 6)");
		languageList.put("35", "JavaScript (rhino 1.7R1-2)");
		languageList.put("26", "Lua (luac 5.1.3)");
		languageList.put("30", "Nemerle (ncc 0.9.3)");
		languageList.put("25", "Nice (nicec 0.9.6)");
		languageList.put("8", "Ocaml (ocamlopt 3.10.2)");
		languageList.put("22", "Pascal (fpc 2.2.4)");
		languageList.put("2", "Pascal (gpc 20070904)");
		languageList.put("3", "Perl (perl 5.12.1)");
		languageList.put("29", "PHP (php 5.2.6)");
		languageList.put("19", "Pike (pike 7.6.112)");
		languageList.put("15", "Prolog (swipl 5.6.58)");
		languageList.put("4", "Python (python 2.7)");
		languageList.put("116", "Python 3 (python 3.2.3)");
		languageList.put("126", "Python 3 nbc (python 3.2.3 nbc)");
		languageList.put("17", "Ruby (ruby 1.9.3)");
		languageList.put("39", "Scala (scala 2.8.0)");
		languageList.put("33", "Scheme (guile 1.8.5)");
		languageList.put("18", "Scheme (stalin 0.11)");
		languageList.put("46", "Sed (sed-4.2)");
		languageList.put("23", "Smalltalk (gst 3.0.3)");
		languageList.put("38", "Tcl (tclsh 8.5.3)");
		languageList.put("42", "TECS ()");
		languageList.put("62", "Text (plain text)");
		languageList.put("6", "Whitespace (wspace 0.3)");
		sc.setAttribute("SPOJ", languageList);
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
		return html.contains("<a href=\"/logout\">");
	}
	
	private void login(String username, String password) throws ClientProtocolException, IOException {
		try {
			post = new HttpPost("/logout");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("login_user", username));
			nvps.add(new BasicNameValuePair("password", password));
			nvps.add(new BasicNameValuePair("autologin", "1"));
			nvps.add(new BasicNameValuePair("submit", "Log In"));
			
			post.setEntity(new UrlEncodedFormEntity(nvps, Consts.ISO_8859_1));
			
			post.addHeader("Host", "www.spoj.com");
			
			response = client.execute(host, post);
			entity = response.getEntity();
			
			html = EntityUtils.toString(entity);
			
			if (!html.contains(username)) {
				throw new RuntimeException();
			}
		} finally {
			EntityUtils.consume(entity);
		}
	}

	private void submit() throws ClientProtocolException, IOException {
		Problem problem = (Problem) baseService.query(Problem.class, submission.getProblem().getId());

		try {
			post = new HttpPost("/submit/complete/");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("lang", submission.getLanguage()));
			nvps.add(new BasicNameValuePair("problemcode", problem.getOriginProb()));
			nvps.add(new BasicNameValuePair("file", submission.getSource()));

			post.setEntity(new UrlEncodedFormEntity(nvps, Consts.ISO_8859_1));

			post.addHeader("Host", "www.spoj.com");

			response = client.execute(host, post);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);

			if (html.contains("submit in this language for this problem")) {
				throw new RuntimeException("judge_exception:Language Error");
			}
			if (html.contains("solution is too long")) {
				throw new RuntimeException("judge_exception:Code length exceeded");
			}
			runId = Tools.regFind(html, "name=\"newSubmissionId\" value=\"(\\d+)\"");
			if (StringUtils.isEmpty(runId)) {
				throw new RuntimeException();
			}
		} finally {
			EntityUtils.consume(entity);
		}
	}

	public void getResult() throws Exception {
		Pattern p = Pattern.compile("\"status_description\":\"(.+?)\", \"id\":" + runId + ", \"status\":.+?,\"time\":\"(.+?)\",\"mem\":\"(.+?)\",");

		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000) {
			try {
				post = new HttpPost("/status/ajax=1,ajaxdiff=1");
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("ids", runId));
				post.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8")));
				post.addHeader("Host", "www.spoj.com");
				response = client.execute(host, post);
				entity = response.getEntity();
				html = EntityUtils.toString(entity);
			} finally {
				EntityUtils.consume(entity);
			}
			html = html.replaceAll("\\\\[nt]", "").replaceAll(">(run|edit)<", "><").replaceAll("<.*?>", "").replace("&nbsp;", "").trim();
			
			Matcher m = p.matcher(html);
			if (m.find()) {
				String result = m.group(1).replace("accepted", "Accepted");
				submission.setStatus(result);
				submission.setRealRunId(runId);
				if (!result.contains("ing")) {
					if (result.equals("Accepted")) {
						int mul = m.group(3).contains("M") ? 1024 : 1;
						submission.setMemory((int) (0.5 + mul * Double.parseDouble(m.group(3).replaceAll("[Mk]", "").trim())));
						submission.setTime((int) (0.5 + 1000 * Double.parseDouble(m.group(2).trim())));
					} else if (result.contains("compilation error")) {
						getAdditionalInfo();
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

	private void getAdditionalInfo() throws HttpException, IOException {
		try {
			get = new HttpGet("/error/" + runId);
			response = client.execute(host, get);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		submission.setAdditionalInfo("<pre>" + Tools.regFind(html, "<div align=\"left\"><pre><small>([\\s\\S]*?)</small></pre>") + "</pre>");
	}

	private int getIdleClient() {
		int length = usernameList.length;
		int begIdx = (int) (Math.random() * length);

		while (true) {
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
			submit();
			errorCode = 2;
			submission.setStatus("Running & Judging");
			baseService.addOrModify(submission);
			getResult();
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage() != null && e.getMessage().startsWith("judge_exception:")) {
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
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} // SPOJ限制每两次提交之间至少隔???秒
		synchronized (using) {
			using[idx] = false;
		}
	}
}
