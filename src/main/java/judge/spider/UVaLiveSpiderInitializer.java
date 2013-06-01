package judge.spider;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.submitter.UVALiveSubmitter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class UVaLiveSpiderInitializer extends Thread {

	public static int threadCnt = 0;
	private static ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<String, Long>();
	private String rootUrl;

	static {
		try {
			Class.forName("judge.submitter.UVALiveSubmitter");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public UVaLiveSpiderInitializer(String url) {
		if (UVALiveSpider.problemNumberMap == null) {
			UVALiveSpider.problemNumberMap = new String[20000];
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
		HttpGet getMethod = new HttpGet(rootUrl);
		HttpClient httpClient = new DefaultHttpClient(UVALiveSubmitter.cm, UVALiveSubmitter.params);
		HttpEntity entity = null;
		try {
			System.out.println("start: " + rootUrl);
			try {
				HttpResponse response = httpClient.execute(getMethod);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) {
					throw new Exception();
				}
				entity = response.getEntity();
				html = EntityUtils.toString(entity);
			} finally {
				EntityUtils.consume(entity);
			}
			html = html.substring(html.indexOf("Total Users / Solving"));

			Matcher matcher = Pattern.compile("category=(\\d+)\">").matcher(html);
			while (matcher.find()) {
				new UVaLiveSpiderInitializer("https://icpcarchive.ecs.baylor.edu/index.php?option=com_onlinejudge&Itemid=8&category=" + matcher.group(1)).start();
			}

			matcher = Pattern.compile("page=show_problem&amp;problem=(\\d+)\">(\\d+)").matcher(html);
			while (matcher.find()) {
				System.out.println(matcher.group(2) + "->" +  matcher.group(1));
				UVALiveSpider.problemNumberMap[Integer.parseInt(matcher.group(2))] = matcher.group(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			--threadCnt;
		}
	}
}
