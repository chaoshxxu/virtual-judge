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

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class AizuSubmitter extends Submitter {

	static final String OJ_NAME = "Aizu";
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
		languageList.put("C++", "C++");
		languageList.put("C", "C");
		languageList.put("JAVA", "JAVA");
		sc.setAttribute("Aizu", languageList);
	}

	private void getMaxRunId() throws Exception {
		GetMethod getMethod = new GetMethod("http://judge.u-aizu.ac.jp/onlinejudge/status.jsp");
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		httpClient.executeMethod(getMethod);
		String html = Tools.getHtml(getMethod, null);
		maxRunId = Integer.parseInt(Tools.regFind(html, "show_code\\.jsp\\?runID=(\\d{6,})"));
		System.out.println("maxRunId : " + maxRunId);
	}

	private void submit(String username, String password) throws Exception{
		PostMethod postMethod = new PostMethod("http://judge.u-aizu.ac.jp/onlinejudge/servlet/Submit");
		postMethod.addParameter("language", submission.getLanguage());
		postMethod.addParameter("password", password);
		postMethod.addParameter("problemNO", submission.getOriginProb());
		postMethod.addParameter("sourceCode", submission.getSource());
		postMethod.addParameter("userID", username);
		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		httpClient.getParams().setContentCharset("UTF-8");

		System.out.println("submit...");
		httpClient.executeMethod(postMethod);
		String html = Tools.getHtml(postMethod, null);
		if (!html.contains("HTTP-EQUIV=\"refresh\"")){
			throw new Exception();
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
		String result;
		Pattern p = Pattern.compile(reg);

		GetMethod getMethod = new GetMethod("http://judge.u-aizu.ac.jp/onlinejudge/status.jsp");
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 300000){
			System.out.println("getResult...");
			httpClient.executeMethod(getMethod);
			String html = Tools.getHtml(getMethod, null);

			Matcher m = p.matcher(html);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId){
				result = m.group(2).replaceAll("<[^<>]*>", "").trim();
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
		System.out.println(s);
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
		GetMethod getMethod = new GetMethod("http://judge.u-aizu.ac.jp/onlinejudge/compile_log.jsp?runID=" + runId);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

		httpClient.executeMethod(getMethod);
		String additionalInfo = Tools.getHtml(getMethod, null);

		submission.setAdditionalInfo(additionalInfo.substring(5 + additionalInfo.indexOf("</h3>")));
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
		}	//aizu貌似不限制每两次提交之间的时间
		synchronized (using) {
			using[idx] = false;
		}
	}

}
