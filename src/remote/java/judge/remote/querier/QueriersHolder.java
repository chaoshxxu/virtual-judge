package judge.remote.querier;

import java.util.HashMap;
import java.util.List;

import judge.remote.RemoteOj;
import judge.tool.SpringBean;
import judge.tool.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueriersHolder {
    private final static Logger log = LoggerFactory.getLogger(QueriersHolder.class);

    private static HashMap<RemoteOj, Querier> queriers = new HashMap<RemoteOj, Querier>();

    public static Querier getQuerier(RemoteOj remoteOj) {
        if (!queriers.containsKey(remoteOj)) {
            synchronized (queriers) {
                if (!queriers.containsKey(remoteOj)) {
                    try {
                        List<Class<? extends Querier>> querierClasses = Tools.findSubClasses("judge", Querier.class);
                        for (Class<? extends Querier> querierClass : querierClasses) {
                            Querier querier = SpringBean.getBean(querierClass);
                            queriers.put(querier.getOjInfo().remoteOj, querier);
                        }
                    } catch (Throwable t) {
                        log.error(t.getMessage(), t);
                        System.exit(-1);
                    }
                }
            }
        }
        return queriers.get(remoteOj);
    }

}
