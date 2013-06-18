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
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class ZOJSubmitter extends Submitter {

	static final String OJ_NAME = "ZOJ";
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
		}

		Map<String, String> languageList = new TreeMap<String, String>();
		languageList.put("1", "C (gcc 4.4.5)");
		languageList.put("2", "C++ (g++ 4.4.5)");
		languageList.put("3", "FPC (fpc 2.4.0)");
		languageList.put("4", "Java (java 1.6.0)");
		languageList.put("5", "Python (Python 2.6.6)");
		languageList.put("6", "Perl (Perl 5.10.1)");
		languageList.put("7", "Scheme (Guile 1.8.7)");
		languageList.put("8", "PHP (PHP 5.3.2)");
		sc.setAttribute("ZOJ", languageList);
	}

	private void getMaxRunId() throws Exception {
		GetMethod getMethod = new GetMethod("http://acm.zju.edu.cn/onlinejudge/showRuns.do?contestId=1");
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		Pattern p = Pattern.compile("<td class=\"runId\">(\\d+)</td>");

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

	private void submit() throws Exception{
		Problem problem = (Problem) baseService.query(Problem.class, submission.getProblem().getId());

		GetMethod getMethod = new GetMethod("http://acm.zju.edu.cn/onlinejudge/showProblem.do?problemCode=" + problem.getOriginProb());
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		int statusCode = httpClient.executeMethod(getMethod);
		byte[] responseBody = getMethod.getResponseBody();
		String tLine = new String(responseBody, "UTF-8");
		String problemId = Tools.regFind(tLine, "problemId=([\\s\\S]*?)\"><font");

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		PostMethod postMethod = new PostMethod("http://acm.zju.edu.cn/onlinejudge/submit.do");
		postMethod.addParameter("languageId", submission.getLanguage());
		postMethod.addParameter("problemId", problemId);
		postMethod.addParameter("source", submission.getSource());
		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		httpClient.getParams().setContentCharset("UTF-8");

		System.out.println("submit...");
		statusCode = httpClient.executeMethod(postMethod);
		System.out.println("statusCode = " + statusCode);
        responseBody = postMethod.getResponseBody();
        tLine = new String(responseBody, "UTF-8");
        if (!tLine.contains("Submit Successfully")){
        	throw new Exception();
        }
	}

	private void login(String username, String password) throws Exception{
        PostMethod postMethod = new PostMethod("http://acm.zju.edu.cn/onlinejudge/login.do");

        postMethod.addParameter("handle", username);
        postMethod.addParameter("password", password);
        postMethod.addParameter("rememberMe", "on");
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		System.out.println("login...");
		int statusCode = httpClient.executeMethod(postMethod);
		System.out.println("statusCode = " + statusCode);
		if (statusCode != HttpStatus.SC_MOVED_TEMPORARILY){
        	throw new Exception();
        }
	}

	public void getResult(String username) throws Exception{
		String reg = "<td class=\"runId\">(\\d+)[\\s\\S]*?judgeReply\\w{2,5}\">([\\s\\S]*?)</span>[\\s\\S]*?runTime\">([\\s\\S]*?)</td>[\\s\\S]*?runMemory\">([\\s\\S]*?)</td>", result;
		Pattern p = Pattern.compile(reg);

		GetMethod getMethod = new GetMethod("http://acm.zju.edu.cn/onlinejudge/showRuns.do?contestId=1&handle=" + username);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			System.out.println("getResult...");
			httpClient.executeMethod(getMethod);
			byte[] responseBody = getMethod.getResponseBody();
			String tLine = new String(responseBody, "UTF-8");

			Matcher m = p.matcher(tLine);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId) {
				result = m.group(2).replaceAll("<[\\s\\S]*?>", "").trim().replaceAll("loating ", "loat-");
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
				if (!result.contains("ing")) {
					if (result.equals("Accepted")) {
						submission.setMemory(Integer.parseInt(m.group(4)));
						submission.setTime(Integer.parseInt(m.group(3)));
					} else if (result.contains("Compilation Error")) {
						String wierdRunId = Tools.regFind(m.group(2), "submissionId=(\\d+)");
						getAdditionalInfo(wierdRunId);
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
		GetMethod getMethod = new GetMethod("http://acm.zju.edu.cn/onlinejudge/showJudgeComment.do?submissionId=" + runId);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

		httpClient.executeMethod(getMethod);
		String additionalInfo = Tools.getHtml(getMethod, null);

		submission.setAdditionalInfo("<pre>" + additionalInfo + "</pre>");
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
		httpClient = clientList[idx];

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
		}	//zju oj限制每两次提交之间至少隔5秒
		synchronized (using) {
			using[idx] = false;
		}
	}


}
