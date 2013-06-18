package judge.submitter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
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

public class URALSubmitter extends Submitter {

	static final String OJ_NAME = "URAL";
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
			clientList[i].getHttpConnectionManager().getParams().setConnectionTimeout(10000);
			clientList[i].getHttpConnectionManager().getParams().setSoTimeout(10000);
//			clientList[i].getHostConfiguration().setProxy("127.0.0.1", 8087);
		}

		Map<String, String> languageList = new TreeMap<String, String>();
		languageList.put("3", "FreePascal 2.0.4");
		languageList.put("9", "Visual C 2010");
		languageList.put("10", "Visual C++ 2010");
		languageList.put("11", "Visual C# 2010");
		languageList.put("12", "Java 1.7");
		languageList.put("14", "Go 1.0.3");
		languageList.put("15", "VB.NET 2010");
		languageList.put("16", "Python 2.7");
		languageList.put("17", "Python 3.3");
		languageList.put("18", "Ruby 1.9.3");
		languageList.put("19", "Haskell 7.6.1");
		languageList.put("20", "GCC 4.7.2");
		languageList.put("21", "G++ 4.7.2");
		languageList.put("22", "GCC 4.7.2 C11");
		languageList.put("23", "G++ 4.7.2 C++11");
		sc.setAttribute("URAL", languageList);
	}


	private void getMaxRunId() throws Exception {
		GetMethod getMethod = new GetMethod("http://acm.timus.ru/status.aspx");
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(6, true));
		Pattern p = Pattern.compile("<TD class=\"id\">(\\d+)");

		byte[] responseBody;
		try {
			httpClient.executeMethod(getMethod);
			responseBody = getMethod.getResponseBody();
		} finally {
			getMethod.releaseConnection();
		}

		String tLine = new String(responseBody, "UTF-8");
		Matcher m = p.matcher(tLine);
		if (m.find()) {
			maxRunId = Integer.parseInt(m.group(1));
			System.out.println("maxRunId : " + maxRunId);
		} else {
			throw new Exception();
		}
	}


	private void submit(String password) throws Exception{
		Problem problem = (Problem) baseService.query(Problem.class, submission.getProblem().getId());

		PostMethod postMethod = new PostMethod("http://acm.timus.ru/submit.aspx");
		postMethod.addParameter("Action", "submit");
		postMethod.addParameter("Language", submission.getLanguage());
		postMethod.addParameter("ProblemNum", problem.getOriginProb());
		postMethod.addParameter("Source", submission.getSource());
		postMethod.addParameter("JudgeID", password);
		postMethod.addParameter("SpaceID", "1");
		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		httpClient.getParams().setContentCharset("UTF-8");

		try {
			System.out.println("submit...");
			int statusCode = httpClient.executeMethod(postMethod);
			System.out.println("statusCode = " + statusCode);
			if (statusCode != HttpStatus.SC_MOVED_TEMPORARILY){
				throw new Exception();
			}
		} finally {
			postMethod.releaseConnection();
		}
	}

	public void getResult(String username) throws Exception{
		GetMethod getMethod = new GetMethod("http://acm.timus.ru/status.aspx?author=" + username.replaceAll("\\D", ""));
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(6, true));
		String reg = "aspx/(\\d+)[\\s\\S]*?class=\"verdict_\\w{2,5}\">([\\s\\S]*?)</TD>[\\s\\S]*?runtime\">([\\d\\.]*)[\\s\\S]*?memory\">([\\d\\s]*)", result;
		Pattern p = Pattern.compile(reg);
		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			System.out.println("getResult...");
			byte[] responseBody;
			try {
				int tryTimes = 6;
				while (tryTimes-- > 0) {
					try {
						httpClient.executeMethod(getMethod);
					} catch (SocketTimeoutException e) {
						System.err.println("Read time out. Try again.");
						continue;
					}
					break;
				}
				responseBody = getMethod.getResponseBody();
			} finally {
				getMethod.releaseConnection();
			}
			String tLine = new String(responseBody, "UTF-8");
			Matcher m = p.matcher(tLine);

			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId){
				result = m.group(2).replaceAll("<[\\s\\S]*?>", "").trim().replace("floating-point", "float-point");
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
				if (!result.contains("ing")){
					if (result.equals("Accepted")){
						submission.setMemory(Integer.parseInt(m.group(4).replaceAll(" ", "")));
						submission.setTime((int)(0.5 + 1000 * Double.parseDouble(m.group(3))));
					} else if (result.contains("Compilation error")) {
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
		GetMethod getMethod = new GetMethod("http://acm.timus.ru/ce.aspx?id=" + runId);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(6, true));

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

		try {
			getMaxRunId();

			submit(passwordList[idx]);	//非登陆式,只需交一次
			errorCode = 2;
			submission.setStatus("Running & Judging");
			baseService.addOrModify(submission);
			Thread.sleep(6000);
			getResult(passwordList[idx]);
		} catch (Exception e) {
			e.printStackTrace();
			submission.setStatus("Judging Error " + errorCode);
			baseService.addOrModify(submission);
		}
	}


	@Override
	public void waitForUnfreeze() {
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	//ural oj限制每两次提交之间至少隔X秒
		synchronized (using) {
			using[idx] = false;
		}
	}

}
