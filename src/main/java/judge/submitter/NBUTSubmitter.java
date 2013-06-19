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

public class NBUTSubmitter extends Submitter {

	static final String OJ_NAME = "NBUT";
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
		languageList.put("1", "GCC");
		languageList.put("2", "G++");
		languageList.put("4", "FPC");
		sc.setAttribute("NBUT", languageList);
	}

	private void getMaxRunId() throws Exception {
		GetMethod getMethod = new GetMethod("http://cdn.ac.nbutoj.com/Problem/status.xhtml");
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		Pattern p = Pattern.compile("<td style=\"text-align: center;\">(\\d+)");

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
        PostMethod postMethod = new PostMethod("http://cdn.ac.nbutoj.com/Problem/submitok.xhtml");
        postMethod.addParameter("language", submission.getLanguage());
        postMethod.addParameter("code", submission.getSource());
        postMethod.addParameter("id", submission.getOriginProb());
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        httpClient.getParams().setContentCharset("UTF-8");
		System.out.println("submit...");
		httpClient.executeMethod(postMethod);
	}

	private void ensureLoggedIn() throws Exception {
		GetMethod getMethod = new GetMethod("http://cdn.ac.nbutoj.com/");
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

		String html = null;
		try {
			httpClient.executeMethod(getMethod);
			html = Tools.getHtml(getMethod, "UTF-8");
		} finally {
			getMethod.releaseConnection();
		}
		if (!html.contains("<a href=\"/User/logout.xhtml\" title=\"登出\">")) {
			Thread.sleep(2000);
			login(usernameList[idx], passwordList[idx]);
		}
	}

	private void login(String username, String password) throws Exception{
		String ojVerify = null;

        GetMethod getMethod = new GetMethod("http://cdn.ac.nbutoj.com/User/login.xhtml?url=%2F");
		try {
			httpClient.executeMethod(getMethod);
			String html = Tools.getHtml(getMethod, "UTF-8");
			ojVerify = Tools.regFind(html, "name=\"__OJVERIFY__\" value=\"(\\w+)\"");
		} finally {
			getMethod.releaseConnection();
		}

        PostMethod postMethod = new PostMethod("http://cdn.ac.nbutoj.com/User/chklogin.xhtml");
        postMethod.addParameter("__OJVERIFY__", ojVerify);
        postMethod.addParameter("password", password);
        postMethod.addParameter("username", username);
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

        System.out.println("login...");
		try {
			httpClient.executeMethod(postMethod);
			String html = Tools.getHtml(postMethod, "UTF-8");
			if (!html.contains("1")) {
				throw new Exception();
			}
		} finally {
			getMethod.releaseConnection();
		}
	}

	public void getResult(String username) throws Exception{
		String reg =
				"<td style=\"text-align: center;\">(\\d+)</td>\\s*" +
				"<td style=\"text-align: center;\">[\\s\\S]*?</td>\\s*" +
				"<td style=\"text-align: center;\">[\\s\\S]*?</td>\\s*" +
				"<td style=\"text-align: center;\">([\\s\\S]*?)</td>\\s*" +
				"<td style=\"text-align: center;\">(\\d+)</td>\\s*" +
				"<td style=\"text-align: center;\">(\\d+)</td>";
		String result;
		Pattern p = Pattern.compile(reg);
		GetMethod getMethod = new GetMethod("http://cdn.ac.nbutoj.com/Problem/status.xhtml?username=" + username);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			System.out.println("getResult...");
			httpClient.executeMethod(getMethod);
			byte[] responseBody = getMethod.getResponseBody();
			String tLine = new String(responseBody, "UTF-8");

			Matcher m = p.matcher(tLine);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId){
				result = toCamel(m.group(2).replaceAll("<.*?>", "").trim());
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
				if (!result.contains("ing")){
					if (result.equals("Accepted")){
						submission.setTime(Integer.parseInt(m.group(3)));
						submission.setMemory(Integer.parseInt(m.group(4)));
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
		GetMethod getMethod = new GetMethod("http://cdn.ac.nbutoj.com/Problem/viewce.xhtml?submitid=" + runId);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

		httpClient.executeMethod(getMethod);
		String additionalInfo = Tools.getHtml(getMethod, "UTF-8");

		submission.setAdditionalInfo(Tools.regFind(additionalInfo, "(<pre style=\"overflow-x: auto;\">[\\s\\S]*?</pre>)"));
	}

	private String toCamel(String string) {
		return (string.charAt(0) + string.substring(1).toLowerCase()).replace("_", " ");
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
			ensureLoggedIn();
			submit();
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
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	//nbut oj限制每两次提交之间至少隔?秒
		synchronized (using) {
			using[idx] = false;
		}
	}

}
