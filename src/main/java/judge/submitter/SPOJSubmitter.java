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

import judge.bean.Problem;
import judge.tool.ApplicationContainer;
import judge.tool.Tools;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class SPOJSubmitter extends Submitter {

	static final String OJ_NAME = "SPOJ";
	static private HttpClient clientList[];
	static private boolean using[];
	static private String[] usernameList;
	static private String[] passwordList;

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
		clientList = new HttpClient[usernameList.length];
		for (int i = 0; i < clientList.length; i++){
			clientList[i] = new HttpClient();
			clientList[i].getParams().setParameter(HttpMethodParams.USER_AGENT, "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8");
			clientList[i].getHttpConnectionManager().getParams().setConnectionTimeout(60000);
			clientList[i].getHttpConnectionManager().getParams().setSoTimeout(60000);
//			clientList[i].getHostConfiguration().setProxy("127.0.0.1", 8087);
		}

		Map<String, String> languageList = new TreeMap<String, String>();
		languageList.put("11", "C (gcc 4.3.2)");
		languageList.put("34", "C99 strict (gcc 4.3.2)");
		languageList.put("1", "C++ (g++ 4.0.0-8)");
		languageList.put("41", "C++ (g++ 4.3.2)");
		languageList.put("2", "Pascal (gpc 20070904)");
		languageList.put("22", "Pascal (fpc 2.2.4)");
		languageList.put("38", "Tcl (tclsh 8.5.3)");
		languageList.put("39", "Scala (Scalac 2.7.4)");
		languageList.put("10", "Java (JavaSE 6)");
		languageList.put("25", "Nice (nicec 0.9.6)");
		languageList.put("24", "JAR (JavaSE 6)");
		languageList.put("27", "C# (gmcs 2.0.1)");
		languageList.put("30", "Nemerle (ncc 0.9.3)");
		languageList.put("23", "Smalltalk (gst 3.0.3)");
		languageList.put("13", "Assembler (nasm 2.03.01)");
		languageList.put("20", "D (gdc 4.1.3)");
		languageList.put("5", "Fortran 95 (gfortran 4.3.2)");
		languageList.put("7", "ADA 95 (gnat 4.3.2)");
		languageList.put("28", "Bash (bash 3.2.29)");
		languageList.put("3", "Perl (perl 5.10.0)");
		languageList.put("44", "Python (python 2.6.2)");
		languageList.put("4", "Python (python 2.5)");
		languageList.put("17", "Ruby (ruby 1.9.0)");
		languageList.put("26", "Lua (luac 5.1.3)");
		languageList.put("16", "Icon (iconc 9.4.3)");
		languageList.put("19", "Pike (pike 7.6.112)");
		languageList.put("29", "PHP (php 5.2.6)");
		languageList.put("33", "Scheme (guile 1.8.5)");
		languageList.put("18", "Scheme (stalin 0.11)");
		languageList.put("31", "Common Lisp (sbcl 1.0.18)");
		languageList.put("32", "Common Lisp (clisp 2.44.1)");
		languageList.put("21", "Haskell (ghc 6.10.4)");
		languageList.put("36", "Erlang (erl 5.6.3)");
		languageList.put("8", "Ocaml (ocamlopt 3.10.2)");
		languageList.put("14", "Clips (clips 6.24)");
		languageList.put("15", "Prolog (swipl 5.6.58)");
		languageList.put("6", "Whitespace (wspace 0.3)");
		languageList.put("12", "Brainf**k (bff 1.0.3.1)");
		languageList.put("9", "Intercal (ick 0.28-4)");
		languageList.put("62", "Text (plain text)");
		languageList.put("35", "JavaScript (rhino 1.7R1-2)");
		sc.setAttribute("SPOJ", languageList);
	}

	private void getMaxRunId() throws Exception {
		// 获取当前最大RunID
		GetMethod getMethod = new GetMethod("http://www.spoj.com/status");
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		Pattern p = Pattern.compile("id=\"max_id\" value=\"(\\d+)");

		httpClient.executeMethod(getMethod);
		byte[] responseBody = getMethod.getResponseBody();
		String tLine = new String(responseBody, "UTF-8");
		Matcher m = p.matcher(tLine);
		if (m.find()) {
			maxRunId = Integer.parseInt(m.group(1));
			System.out.println("maxRunId : " + maxRunId);
		} else {
			throw new Exception();
		}
	}


	private void submit(String username, String password) throws Exception{
		Problem problem = (Problem) baseService.query(Problem.class, submission.getProblem().getId());
		PostMethod postMethod = new PostMethod("http://www.spoj.com/submit/complete/");

		postMethod.addParameter("lang", submission.getLanguage());
		postMethod.addParameter("login_user", username);
		postMethod.addParameter("password", password);
		postMethod.addParameter("problemcode", problem.getOriginProb());
		postMethod.addParameter("file", submission.getSource());
		postMethod.addParameter("submit", "Send");
		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		httpClient.getParams().setContentCharset("UTF-8");

		System.out.println("submit...");
		int statusCode = httpClient.executeMethod(postMethod);
		System.out.println("statusCode = " + statusCode);
		byte[] responseBody = postMethod.getResponseBody();
		String tLine = new String(responseBody, "UTF-8");
		if (tLine.contains("submit in this language for this problem")){
			throw new Exception("judge_exception:Language Error");
		}
		if (tLine.contains("solution is too long")){
			throw new Exception("judge_exception:Code length exceeded");
		}
		//注意:此处判断登陆成功条件并不充分,相当于默认成功
	}

	public void getResult(String username) throws Exception{
		String reg = "id=\"max_id\" value=\"(\\d+)[\\s\\S]*?<td class=\"statusres\"[\\s\\S]*?>([\\s\\S]*?)</td>\n<td[\\s\\S]*?>([\\s\\S]*?)</td>\n<td[\\s\\S]*?>([\\s\\S]*?)</td>", result;
		Pattern p = Pattern.compile(reg);
		GetMethod getMethod = new GetMethod("http://www.spoj.com/status/" + username);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			System.out.println("getResult...");
			httpClient.executeMethod(getMethod);
			byte[] responseBody = getMethod.getResponseBody();
			String tLine = new String(responseBody, "UTF-8");
			Matcher m = p.matcher(tLine);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId){
				result = m.group(2).replaceAll("edit", "").replaceAll(">run<", "><").replaceAll("<[\\s\\S]*?>", "").replaceAll("&nbsp;", "").trim();
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
				if (!result.contains("ing")){
					if (result.contains("accepted")){
						submission.setStatus("Accepted");
						result = m.group(4).trim();
						int mul = result.contains("M") ? 1024 : 1;
						submission.setMemory((int)(0.5 + mul * Double.parseDouble(result.replaceAll("[Mk]", ""))));
						submission.setTime((int)(0.5 + 1000 * Double.parseDouble(m.group(3).replaceAll("<[\\s\\S]*?>", "").trim())));
					} else if (result.contains("compilation error")) {
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
		GetMethod getMethod = new GetMethod("http://www.spoj.com/error/" + runId);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

		httpClient.executeMethod(getMethod);
		String additionalInfo = Tools.getHtml(getMethod, null);

		submission.setAdditionalInfo("<pre>" + Tools.regFind(additionalInfo, "<div align=\"left\"><pre><small>([\\s\\S]*?)</small></pre>") + "</pre>");
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

			submit(usernameList[idx], passwordList[idx]);	//非登陆式,只需交一次
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
		}	//client冷却时间
		synchronized (using) {
			using[idx] = false;
		}
	}

}
