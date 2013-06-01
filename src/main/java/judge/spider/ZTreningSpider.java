package judge.spider;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.tool.Tools;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


public class ZTreningSpider extends Spider {

	private static Set<String> validIds;
	public static Long lastTime = 0L;

	public void crawl() throws Exception{
		if (new Date().getTime() - lastTime > 7 * 86400 * 1000L) {
			validIds = null;
			lastTime = new Date().getTime();
		}
		if (validIds == null) {
			validIds = new HashSet<String>();
			initValidIds("http://www.z-trening.com/training.php?all_tasks=1");
			initValidIds("http://www.z-trening.com/training.php?all_user_tasks=1");
		}
		while (!validIds.contains("1437")) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		//这货居然还记录非法的题目访问，故为保险起见，先初始化抓取合法的题号集合，在vj服务器端判断题号是否合法
		if (!validIds.contains(problem.getOriginProb())) {
			throw new Exception();
		}

		String html = "";
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod("http://www.z-trening.com/tasks.php?show_task=" + (5000000000L + Integer.parseInt(problem.getOriginProb())) + "&lang=uk");
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if(statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: "+getMethod.getStatusLine());
			}
			html = Tools.getHtml(getMethod, null);
		} catch(Exception e) {
			getMethod.releaseConnection();
			throw new Exception();
		}

		if (html.contains("<H1>Error</H1>")) {
			throw new Exception();
		}

		problem.setTitle(Tools.regFind(html, "<TITLE>Task :: ([\\s\\S]*?)</TITLE>").trim());
		if (problem.getTitle().isEmpty()){
			throw new Exception();
		}
		Double timeLimit = 1000 * Double.parseDouble(Tools.regFind(html, "Time:</TD><TD CLASS=\"right\">(\\S*?) sec"));
		problem.setTimeLimit(timeLimit.intValue());
		problem.setMemoryLimit(1024 * Integer.parseInt(Tools.regFind(html, "Memory:</TD><TD CLASS=\"right\">(\\d+) MB")));
		description.setDescription(Tools.regFind(html, "<DIV CLASS=\"taskText\">([\\s\\S]*?)</DIV></DIV><DIV CLASS=\"boxHeader\">Submit Solution"));
		problem.setUrl("http://www.z-trening.com/tasks.php?show_task=" + (5000000000L + Integer.parseInt(problem.getOriginProb())) + "&lang=uk");
	}

	private void initValidIds(String url) throws HttpException, IOException {
		GetMethod getMethod = new GetMethod(url);
		HttpClient httpClient = new HttpClient();
		httpClient.executeMethod(getMethod);
		String html = new String(getMethod.getResponseBody(), "UTF-8");
		Matcher matcher = Pattern.compile("show_task=(\\d+)").matcher(html);
		while (matcher.find()) {
			validIds.add(Long.parseLong(matcher.group(1)) - 5000000000L + "");
		}
	}
}
