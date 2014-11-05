package judge.remote.language;

import java.util.HashMap;
import java.util.List;

import judge.remote.RemoteOj;
import judge.tool.SpringBean;
import judge.tool.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanguageFindersHolder {
    private final static Logger log = LoggerFactory.getLogger(LanguageFindersHolder.class);

    private static final HashMap<RemoteOj, LanguageFinder> finders = new HashMap<>();

    public static LanguageFinder getLanguageFinder(RemoteOj remoteOj) {
        if (!finders.containsKey(remoteOj)) {
            synchronized (finders) {
                if (!finders.containsKey(remoteOj)) {
                    try {
                        List<Class<? extends LanguageFinder>> crawlerClasses = Tools.findSubClasses("judge", LanguageFinder.class);
                        for (Class<? extends LanguageFinder> crawlerClass : crawlerClasses) {
                            LanguageFinder crawler = SpringBean.getBean(crawlerClass);
                            finders.put(crawler.getOjInfo().remoteOj, crawler);
                        }
                    } catch (Throwable t) {
                        log.error(t.getMessage(), t);
                        System.exit(-1);
                    }
                }
            }
        }
        return finders.get(remoteOj);
    }

}
