package judge.spider;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.tool.Tools;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class UVaSpiderInitializer extends Thread {

	public static int threadCnt = 0;
	private static ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<String, Long>();
	private String rootUrl;

	public UVaSpiderInitializer(String url) {
		if (UVASpider.problemNumberMap == null) {
			UVASpider.problemNumberMap = new String[20000];
		}
		rootUrl = url;
	}

	public void run() {
		Long lastTime = map.get(rootUrl);
		if (lastTime == null) {
			lastTime = 0L;
		}
		if (new Date().getTime() - lastTime > 300000L) {
			map.put(rootUrl, new Date().getTime());
		} else {
			return;
		}
		++threadCnt;

		String html = null;
		GetMethod getMethod = new GetMethod(rootUrl);
		HttpClient httpClient = new HttpClient();
		httpClient.getHostConfiguration().setProxy("127.0.0.1", 8087);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(30, true));
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + getMethod.getStatusLine());
				throw new Exception();
			}
			html = Tools.getHtml(getMethod, null);
			html = html.substring(html.indexOf("Total Users / Solving"));

			Matcher matcher = Pattern.compile("category=(\\d+)\">").matcher(html);
			while (matcher.find()) {
				new UVaSpiderInitializer("http://uva.onlinejudge.org/index.php?option=com_onlinejudge&Itemid=8&category=" + matcher.group(1)).start();
			}

			matcher = Pattern.compile("page=show_problem&amp;problem=(\\d+)\">(\\d+)").matcher(html);
			while (matcher.find()) {
				System.out.println(matcher.group(2) + "->" +  matcher.group(1));
				UVASpider.problemNumberMap[Integer.parseInt(matcher.group(2))] = matcher.group(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			getMethod.releaseConnection();
			--threadCnt;
		}
	}
}
