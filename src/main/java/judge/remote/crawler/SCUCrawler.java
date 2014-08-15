package judge.remote.crawler;

import judge.executor.ExecutorTaskType;
import judge.executor.Task;
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.remote.crawler.common.Crawler;
import judge.remote.crawler.common.RawProblemInfo;
import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;

public class SCUCrawler implements Crawler {

	public RawProblemInfo crawl(final String problemId) throws Exception {
		Validate.isTrue(problemId.matches("[1-9]\\d*"));
		final HttpHost host = new HttpHost("cstest.scu.edu.cn");
		final DedicatedHttpClient client = new DedicatedHttpClient(host);

		final String outerUrl = host.toURI() + "/soj/problem.action?id=" + problemId;
		Task<String> taskOuter = new Task<String>(ExecutorTaskType.GENERAL) {
			@Override
			public String call() throws Exception {
				return client.get(outerUrl, HttpStatusValidator.SC_OK).getBody();
			}
		};

		Task<String> taskInner = new Task<String>(ExecutorTaskType.GENERAL) {
			@Override
			public String call() throws Exception {
				String url = host.toURI() + "/soj/problem/" + problemId + "/";
				String html = client.get(url, HttpStatusValidator.SC_OK).getBody();
				return HtmlHandleUtil.transformUrlToAbs(html, url);
			}
		};
		
		taskOuter.submit();
		taskInner.submit();

		RawProblemInfo info = new RawProblemInfo();
		info.url = outerUrl;
		info.title = Tools.regFind(taskOuter.get(), "<title>\\d+: (.+)</title>");
		info.description = taskInner.get();
		
		Validate.isTrue(!StringUtils.isBlank(info.title));

		return info;
	}

	@Override
	public String getOjName() {
		return "SCU";
	}

}
