package judge.remote.querier.common;

import java.util.HashMap;
import java.util.List;

import judge.remote.RemoteOj;
import judge.tool.SpringBean;
import judge.tool.Tools;

public class QueriersHolder {

    private static HashMap<RemoteOj, Querier> queriers = new HashMap<RemoteOj, Querier>();

    public static Querier getQuerier(RemoteOj remoteOj) {
        if (!queriers.containsKey(remoteOj)) {
            synchronized (queriers) {
                if (!queriers.containsKey(remoteOj)) {
                    try {
                        List<Class<? extends Querier>> querierClasses = Tools.findSubClasses("judge/remote/querier", Querier.class);
                        for (Class<? extends Querier> querierClass : querierClasses) {
                            Querier querier = SpringBean.getBean(querierClass);
                            queriers.put(querier.getOj(), querier);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        System.exit(-1);
                    }
                }
            }
        }
        return queriers.get(remoteOj);
    }

}
