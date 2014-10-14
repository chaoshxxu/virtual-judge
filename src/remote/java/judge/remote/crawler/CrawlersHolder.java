package judge.remote.crawler;

import java.util.HashMap;
import java.util.List;

import judge.remote.RemoteOj;
import judge.tool.SpringBean;
import judge.tool.Tools;

public class CrawlersHolder {

    private static HashMap<RemoteOj, Crawler> crawlers = new HashMap<RemoteOj, Crawler>();

    public static Crawler getCrawler(RemoteOj remoteOj) {
        if (!crawlers.containsKey(remoteOj)) {
            synchronized (crawlers) {
                if (!crawlers.containsKey(remoteOj)) {
                    try {
                        List<Class<? extends Crawler>> crawlerClasses = Tools.findSubClasses("judge", Crawler.class);
                        for (Class<? extends Crawler> crawlerClass : crawlerClasses) {
                            Crawler crawler = SpringBean.getBean(crawlerClass);
                            crawlers.put(crawler.getOjInfo().remoteOj, crawler);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        System.exit(-1);
                    }
                }
            }
        }
        return crawlers.get(remoteOj);
    }

}
