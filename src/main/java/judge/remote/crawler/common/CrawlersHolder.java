package judge.remote.crawler.common;

import java.util.HashMap;
import java.util.List;

import judge.tool.Tools;

public class CrawlersHolder {

	private static HashMap<String, Crawler> crawlers = new HashMap<String, Crawler>();

	public static Crawler getCrawler(String ojName) {
		if (crawlers.isEmpty()) {
			synchronized (crawlers) {
				if (crawlers.isEmpty()) {
					try {
						List<Class<? extends Crawler>> crawlerClasses = Tools.findSubClasses("judge/remote/crawler", Crawler.class);
						for (Class<? extends Crawler> crawlerClass : crawlerClasses) {
							Crawler crawler = crawlerClass.newInstance();
							crawlers.put(crawler.getOjName(), crawler);
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}
		}
		return crawlers.get(ojName);
	}

}
