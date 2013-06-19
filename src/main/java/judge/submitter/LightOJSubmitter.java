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

public class LightOJSubmitter extends Submitter {

	static final String OJ_NAME = "LightOJ";
	static private HttpClient clientList[];
	static private boolean using[];
	static public  String[] usernameList;
	static public String[] passwordList;

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
		languageList.put("C", "C");
		languageList.put("C++", "C++");
		languageList.put("JAVA", "JAVA");
		languageList.put("PASCAL", "PASCAL");
		languageList.put("PYTHON", "PYTHON");
		sc.setAttribute("LightOJ", languageList);
	}


	private void getMaxRunId() throws Exception {
		GetMethod getMethod = new GetMethod("http://www.lightoj.com/volume_submissions.php");
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		Pattern p = Pattern.compile("<th class=\"newone\">(\\d+)");

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

        PostMethod postMethod = new PostMethod("http://www.lightoj.com/volume_submit.php");
        postMethod.addParameter("language", submission.getLanguage());
        postMethod.addParameter("sub_problem", problem.getOriginProb());
        postMethod.addParameter("code", submission.getSource());
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        httpClient.getParams().setContentCharset("UTF-8");

        System.out.println("submit...");
		httpClient.executeMethod(postMethod);

		String html = Tools.getHtml(postMethod, null);
		if (!html.contains("location.href='volume_usersubmissions.php'")){
			throw new Exception();
		}
	}

	private void login(String username, String password) throws Exception{
        PostMethod postMethod = new PostMethod("http://www.lightoj.com/login_check.php");

        postMethod.addParameter("mypassword", password);
        postMethod.addParameter("myrem", "1");
        postMethod.addParameter("myuserid", username);
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

        System.out.println("login...");
		httpClient.executeMethod(postMethod);
	}

	public void getResult(String username) throws Exception{
		String reg = "newone[\\s\\S]*?sub_id=(\\d+)[\\s\\S]*?<td class=\"newone\">[\\s\\S]*?<td class=\"newone\">[\\s\\S]*?<td class=\"newone\">\\s*([-\\d\\.]+)[\\s\\S]*?<td class=\"newone\">\\s*([-\\d\\.]+)[\\s\\S]*?<td class=\"newone\">([\\s\\S]*?)</td>", result;
		Pattern p = Pattern.compile(reg);
		GetMethod getMethod = new GetMethod("http://www.lightoj.com/volume_usersubmissions.php");
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			System.out.println("getResult...");
			httpClient.executeMethod(getMethod);
			byte[] responseBody = getMethod.getResponseBody();
			String tLine = new String(responseBody, "UTF-8");

			Matcher m = p.matcher(tLine);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId) {
				result = m.group(4).replaceAll("<[\\s\\S]*?>", "").trim();
				if ("Not Judged Yet".equals(result)) {
					result = "Judging";
				}
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
    			if (!result.contains("ing")){
    				if (result.equals("Accepted")){
	    				submission.setTime((int)(1000 * Double.parseDouble(m.group(2))));
	    				submission.setMemory(Integer.parseInt(m.group(3)));
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
		GetMethod getMethod = new GetMethod("http://www.lightoj.com/volume_showcode.php?sub_id=" + runId);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

		httpClient.executeMethod(getMethod);
		String additionalInfo = Tools.getHtml(getMethod, null);

		submission.setAdditionalInfo("<pre>" + Tools.regFind(additionalInfo, "<textarea style=[^>]+>([\\s\\S]*?)</textarea>") + "</pre>");
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
			try {
				//第一次获取maxRunId + 尝试提交
				getMaxRunId();
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
		}	//hdu oj限制每两次提交之间至少隔5秒
		synchronized (using) {
			using[idx] = false;
		}
	}

}
