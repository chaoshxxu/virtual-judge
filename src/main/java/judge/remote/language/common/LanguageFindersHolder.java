package judge.remote.language.common;

import java.util.HashMap;
import java.util.List;

import judge.remote.RemoteOj;
import judge.tool.SpringBean;
import judge.tool.Tools;

public class LanguageFindersHolder {

	private static HashMap<RemoteOj, LanguageFinder> finders = new HashMap<RemoteOj, LanguageFinder>();

	public static LanguageFinder getLanguageFinder(RemoteOj remoteOj) {
		if (!finders.containsKey(remoteOj)) {
			synchronized (finders) {
				if (!finders.containsKey(remoteOj)) {
					try {
						List<Class<? extends LanguageFinder>> crawlerClasses = Tools.findSubClasses("judge/remote/language", LanguageFinder.class);
						for (Class<? extends LanguageFinder> crawlerClass : crawlerClasses) {
							LanguageFinder crawler = SpringBean.getBean(crawlerClass);
							finders.put(crawler.getOj(), crawler);
						}
					} catch (Throwable t) {
						t.printStackTrace();
						System.exit(-1);
					}
				}
			}
		}
		return finders.get(remoteOj);
	}

}
