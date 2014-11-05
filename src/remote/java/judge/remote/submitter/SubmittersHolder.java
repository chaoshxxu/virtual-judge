package judge.remote.submitter;

import java.util.HashMap;
import java.util.List;

import judge.remote.RemoteOj;
import judge.tool.SpringBean;
import judge.tool.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubmittersHolder {
    private final static Logger log = LoggerFactory.getLogger(SubmittersHolder.class);

    private static HashMap<RemoteOj, Submitter> submitters = new HashMap<RemoteOj, Submitter>();

    public static Submitter getSubmitter(RemoteOj remoteOj) {
        if (!submitters.containsKey(remoteOj)) {
            synchronized (submitters) {
                if (!submitters.containsKey(remoteOj)) {
                    try {
                        List<Class<? extends Submitter>> submitterClasses = Tools.findSubClasses("judge", Submitter.class);
                        for (Class<? extends Submitter> submitterClass : submitterClasses) {
                            Submitter submitter = SpringBean.getBean(submitterClass);
                            submitters.put(submitter.getOjInfo().remoteOj, submitter);
                        }
                    } catch (Throwable t) {
                        log.error(t.getMessage(), t);
                        System.exit(-1);
                    }
                }
            }
        }
        return submitters.get(remoteOj);
    }

}
