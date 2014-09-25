package judge.remote.crawler;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.tool.HtmlHandleUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;

public abstract class SimpleCrawler extends SyncCrawler {

    @Override
    protected RawProblemInfo crawl(String problemId) throws Exception {
        preValidate(problemId);
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getHost(), getCharset());

        String problemUrl = getProblemUrl(problemId);
        String pageContent = client.get(problemUrl, HttpStatusValidator.SC_OK).getBody();
        if (autoTransformAbsoluteHref()) {
            pageContent = HtmlHandleUtil.transformUrlToAbs(pageContent, problemUrl);
        }

        RawProblemInfo info = new RawProblemInfo();
        info.url = problemUrl;
        populateProblemInfo(info, problemId, pageContent);
        
        Validate.isTrue(!StringUtils.isBlank(info.title));
        Validate.isTrue(info.timeLimit > 2);
        
        return info;
    }
    
    /**
     * Can be overridden
     * @return
     */
    protected HttpHost getHost() {
        return getOj().mainHost;
    }
    
    /**
     * Can be overridden
     * @return
     */
    protected String getCharset() {
        return getOj().defaultChaset;
    }

    protected abstract String getProblemUrl(String problemId);
    protected void preValidate(String problemId) {}
    protected boolean autoTransformAbsoluteHref() {
        return true;
    }
    
    /**
     * <pre>Responsible to set the following of info:
     * title
     * timeLimit
     * memoryLimit
     * description
     * input
     * output
     * sampleInput
     * sampleOutput
     * hint
     * source
     * 
     * It also has chance to override existing URL.
     * </pre>
     * 
     * @param info
     * @param problemId
     * @param html
     */
    protected abstract void populateProblemInfo(RawProblemInfo info, String problemId, String html) throws Exception;


    
}
