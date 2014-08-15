package judge.remote.crawler.common;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.tool.HtmlHandleUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;

public abstract class SimpleCrawler implements Crawler {
	
	@Override
	public RawProblemInfo crawl(String problemId) throws Exception {
		preValidate(problemId);
		HttpHost host = getHost();
		DedicatedHttpClient client = new DedicatedHttpClient(host);

		String problemUrl = getProblemUrl(problemId);
		String pageContent = client.get(problemUrl, HttpStatusValidator.SC_OK).getBody();
		if (autoTransformAbsoluteHref()) {
//			System.out.println(pageContent);
			pageContent = HtmlHandleUtil.transformUrlToAbs(pageContent, problemUrl);
//			System.out.println("\n\n------------------------------------\n\n");
//			System.out.println(pageContent);
		}

		RawProblemInfo info = new RawProblemInfo();
		info.url = problemUrl;
		populateProblemInfo(info, problemId, pageContent);
		
		Validate.isTrue(!StringUtils.isBlank(info.title));
		Validate.isTrue(info.timeLimit > 2);

		return info;
	}
	
	protected abstract HttpHost getHost();
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
