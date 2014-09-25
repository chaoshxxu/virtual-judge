package judge.remote.crawler;

import judge.remote.RemoteOjAware;
import judge.tool.Handler;

/**
 * Implementation should be stateless.
 * @author Isun
 *
 */
public interface Crawler extends RemoteOjAware {
    
    /**
     * 
     * @param problemId 
     * @param handler
     * @throws Exception
     */
    void crawl(String problemId, Handler<RawProblemInfo> handler) throws Exception;

}
