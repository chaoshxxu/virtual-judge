package judge.submitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.tool.ApplicationContainer;
import judge.tool.Tools;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

@SuppressWarnings("deprecation")
public class ZTreningSubmitter extends Submitter {

	static final String OJ_NAME = "Z-Trening";
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
			clientList[i].getParams().setParameter(HttpMethodParams.USER_AGENT, "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.17) Gecko/20110420 Firefox/3.6.17");
			clientList[i].getHttpConnectionManager().getParams().setConnectionTimeout(60000);
			clientList[i].getHttpConnectionManager().getParams().setSoTimeout(60000);
//			clientList[i].getHostConfiguration().setProxy("127.0.0.1", 8087);
		}

		Map<String, String> languageList = new TreeMap<String, String>();
		languageList.put("1", "Pascal");
		languageList.put("2", "C");
		languageList.put("3", "C++");
		languageList.put("4", "Java");
		languageList.put("5", "Python");
		languageList.put("6", "PHP");
		languageList.put("7", "Perl");
		languageList.put("8", "Ruby");
		languageList.put("9", "Scheme");
		languageList.put("10", "Fortran");
		languageList.put("11", "YBASIC");
		languageList.put("12", "GPC");
		sc.setAttribute("Z-Trening", languageList);
	}

	private void submit() throws Exception{
		File file = new File(idx + "");
		try{
			FileWriter filewriter = new FileWriter(file, false);
			filewriter.write(submission.getSource());
			filewriter.close();
		}catch(IOException e){
			throw e;
		}catch (Exception ex) {
			throw ex;
		}

		MultipartPostMethod postMethod = new MultipartPostMethod("http://www.z-trening.com/submit_ajax.php?submit=" + (5000000000L + Integer.parseInt(submission.getOriginProb())));
		postMethod.addParameter("submit_lang", submission.getLanguage());
		postMethod.addParameter("source_file", file);
		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		httpClient.getParams().setContentCharset("UTF-8");

		System.out.println("submit...");
		int statusCode = httpClient.executeMethod(postMethod);
		System.out.println("statusCode = " + statusCode);

		String content = new String(postMethod.getResponseBody(), "UTF-8");
		if (content.contains("<P>Please login!</P>")){
			throw new Exception();
		}

		String fetchCode = Tools.regFind(content, "'(\\w+)'\\);");
		GetMethod getMethod = new GetMethod("http://www.z-trening.com/submit_ajax.php?wait_grade=" + fetchCode);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		HttpClient client = new HttpClient();
		client.setTimeout(300000);
		client.executeMethod(getMethod);
		String result = new String(getMethod.getResponseBody(), "UTF-8");

		if (result.contains("Compile ERROR")) {
			submission.setStatus("Compile Error");
			baseService.addOrModify(submission);
			return;
		}

		Matcher matcher = Pattern.compile("Time: (\\S+)s<br> Memory: (\\S+) MB").matcher(result);
		if (matcher.find()) {
			Double time = Double.parseDouble(matcher.group(1)) * 1000;
			Double memory = Double.parseDouble(matcher.group(2)) * 1024;
			submission.setTime(time.intValue());
			submission.setMemory(memory.intValue());
			submission.setStatus("Accepted");
			baseService.addOrModify(submission);
			return;
		}

		matcher = Pattern.compile("(dfailed|dwrong)[\\s\\S]*?div(\\d+)[\\s\\S]*?<P CLASS=\"smallerText\">(.*?)</P>").matcher(result);
		matcher.find();
		if (matcher.group(1).equals("dwrong")) {
			submission.setStatus("WA on test " + matcher.group(2));
		} else {
			submission.setStatus(matcher.group(3) + " on test " + matcher.group(2));
		}
		baseService.addOrModify(submission);
	}

	private void login(String username, String password) throws Exception{
		PostMethod postMethod = new PostMethod("http://www.z-trening.com/index.php?login=1");
		postMethod.addParameter("username", username);
		postMethod.addParameter("password", password);
		postMethod.addParameter("remember", "remember");
		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

		System.out.println("login...");
		int statusCode = httpClient.executeMethod(postMethod);
		System.out.println("statusCode = " + statusCode);
		String content = new String(postMethod.getResponseBody(), "UTF-8");
		if (!content.contains(">My Profile<")) {
			throw new Exception();
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
		int errorCode = 2;

		try {
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
		}	//Z-Trening貌似不限制每两次提交之间的提交间隔
		synchronized (using) {
			using[idx] = false;
		}
	}

}
