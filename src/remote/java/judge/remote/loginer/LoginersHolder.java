package judge.remote.loginer;

import java.util.HashMap;
import java.util.List;

import judge.remote.RemoteOj;
import judge.tool.SpringBean;
import judge.tool.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginersHolder {
    private final static Logger log = LoggerFactory.getLogger(LoginersHolder.class);

    private static HashMap<RemoteOj, Loginer> loginers = new HashMap<RemoteOj, Loginer>();

    public static Loginer getLoginer(RemoteOj remoteOj) {
        if (!loginers.containsKey(remoteOj)) {
            synchronized (loginers) {
                if (!loginers.containsKey(remoteOj)) {
                    try {
                        List<Class<? extends Loginer>> loginerClasses = Tools.findSubClasses("judge", Loginer.class);
                        for (Class<? extends Loginer> loginerClass : loginerClasses) {
                            Loginer loginer = SpringBean.getBean(loginerClass);
                            loginers.put(loginer.getOjInfo().remoteOj, loginer);
                        }
                    } catch (Throwable t) {
                        log.error(t.getMessage(), t);
                        System.exit(-1);
                    }
                }
            }
        }
        return loginers.get(remoteOj);
    }

}
