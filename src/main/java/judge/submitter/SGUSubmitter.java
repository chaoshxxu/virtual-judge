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

public class SGUSubmitter extends Submitter {

	static final String OJ_NAME = "SGU";
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
		languageList.put("GNU C (MinGW, GCC 4)", "GNU C (MinGW, GCC 4)");
		languageList.put("GNU CPP (MinGW, GCC 4)", "GNU CPP (MinGW, GCC 4)");
		languageList.put("Visual Studio C++ 2010", "Visual Studio C++ 2010");
		languageList.put("C# (Mono gmcs 2.4)", "C# (Mono gmcs 2.4)");
		languageList.put("Visual Studio C 2010", "Visual Studio C 2010");
		languageList.put("JAVA 1.6", "JAVA 1.6");
		languageList.put("Delphi 7.0", "Delphi 7.0");
		sc.setAttribute("SGU", languageList);
	}

	private void getMaxRunId() throws Exception {
		// 获取当前最大RunID
		GetMethod getMethod = new GetMethod("http://acm.sgu.ru/status.php");
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		Pattern p = Pattern.compile("<TD>(\\d{7,})</TD>");

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
        PostMethod postMethod = new PostMethod("http://acm.sgu.ru/sendfile.php?contest=0");
        postMethod.addParameter("elang", submission.getLanguage());
        postMethod.addParameter("id", username);
        postMethod.addParameter("pass", password);
        postMethod.addParameter("problem", problem.getOriginProb());
        postMethod.addParameter("source", submission.getSource());
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        httpClient.getParams().setContentCharset("UTF-8");

        System.out.println("submit...");
		httpClient.executeMethod(postMethod);
		byte[] responseBody = postMethod.getResponseBody();
		String tLine = new String(responseBody, "UTF-8");
		if (tLine.contains("Your source seems to be dangerous")) {
			throw new Exception("judge_exception:Dangerous Code Error");
		}
		if (!tLine.contains("successfully submitted")){
			throw new Exception();
		}
	}

	public void getResult(String username) throws Exception{
		String reg = "<TD>(\\d{7,})</TD>[\\s\\S]*?<TD class=btab>([\\s\\S]*?)</TD>[\\s\\S]*?([\\d]*?) ms</TD><TD>([\\d]*?) kb</TD>", result;
		Pattern p = Pattern.compile(reg);

		GetMethod getMethod = new GetMethod("http://acm.sgu.ru/status.php?idmode=1&id=" + username);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			System.out.println("getResult...");
			httpClient.executeMethod(getMethod);
			byte[] responseBody = getMethod.getResponseBody();
			String tLine = new String(responseBody, "UTF-8");

			Matcher m = p.matcher(tLine);
    		if (m.find() && Integer.parseInt(m.group(1)) > maxRunId){
    			result = m.group(2).replaceAll("<[\\s\\S]*?>", "").trim();
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
    			if (!result.contains("ing")){
    				if (result.equals("Accepted")){
	    				submission.setMemory(Integer.parseInt(m.group(4)));
	    				submission.setTime(Integer.parseInt(m.group(3)));
    				} else if (result.contains("Compilation Error")) {
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
		GetMethod getMethod = new GetMethod("http://acm.sgu.ru/cerror.php?id=" + runId);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

		httpClient.executeMethod(getMethod);
		String additionalInfo = Tools.getHtml(getMethod, null);

		submission.setAdditionalInfo(Tools.regFind(additionalInfo, runId + "</TD><TD>(<pre>[\\s\\S]*?</pre>)"));
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
			Thread.sleep(35000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	//sgu限制每两次提交之间至少隔30秒
		synchronized (using) {
			using[idx] = false;
		}
	}

}
