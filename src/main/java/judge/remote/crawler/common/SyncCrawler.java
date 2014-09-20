package judge.remote.crawler.common;

import org.springframework.beans.factory.annotation.Autowired;

import judge.httpclient.DedicatedHttpClientFactory;
import judge.tool.Handler;

/**
 * No dependence on any other resource, just do it in invoking thread.
 * 
 * @author Isun
 */
public abstract class SyncCrawler implements Crawler {
    
    @Autowired
    protected DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public void crawl(String problemId, Handler<RawProblemInfo> handler) throws Exception {
        RawProblemInfo info = null;
        try {
            info = crawl(problemId);
        } catch (Throwable t) {
            handler.onError(t);
            return;
        }
        handler.handle(info);
    }

    abstract protected RawProblemInfo crawl(String problemId) throws Exception;

}
