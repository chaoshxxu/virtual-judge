package judge.remote.crawler;

import java.util.List;

import judge.account.RemoteAccount;
import judge.account.RemoteAccountTask;
import judge.executor.ExecutorTaskType;
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.remote.crawler.common.Crawler;
import judge.remote.crawler.common.RawProblemInfo;
import judge.remote.loginer.LightOJLoginer;
import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;

public class LightOJCrawler implements Crawler {
	
	@Override
	public RawProblemInfo crawl(String problemId) throws Exception {
		Validate.isTrue(problemId.matches("[1-9]\\d*"));
		return new CrawlTask(problemId, ExecutorTaskType.UPDATE_PROBLEM_INFO, getOjName(), null, null).get();
	}
	
	class CrawlTask extends RemoteAccountTask<RawProblemInfo> {
		private String problemId;

		public CrawlTask(String problemId, ExecutorTaskType executorTaskType, String remoteOj, String accountId, String exclusiveCode) {
			super(executorTaskType, remoteOj, accountId, exclusiveCode);
			this.problemId = problemId;
		}

		@Override
		protected RawProblemInfo call(RemoteAccount remoteAccount) {
			new LightOJLoginer().login(remoteAccount);
			
			HttpHost host = new HttpHost("lightoj.com");
			DedicatedHttpClient client = new DedicatedHttpClient(host, remoteAccount.getContext());
			
			String problemUrl = host.toURI() + "/volume_showproblem.php?problem=" + problemId;
			String pageContent = client.get(problemUrl, HttpStatusValidator.SC_OK).getBody();
			pageContent = HtmlHandleUtil.transformUrlToAbs(pageContent, problemUrl);

			RawProblemInfo info = new RawProblemInfo();
			info.url = problemUrl;
			populateProblemInfo(info, problemId, pageContent);

			return info;
		}
	}
	
	protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
		info.title = Tools.regFind(html, "Problem \\d+ - ([\\s\\S]*?)</title>").trim();
		info.timeLimit = ((int)(1000 * Double.parseDouble(Tools.regFind(html, "([\\d\\.]*?) second\\(s\\)</span>"))));
		info.memoryLimit = (1024 * Integer.parseInt(Tools.regFind(html, "([\\d\\.]*?) MB</span>")));

		List<String> styleSheets = HtmlHandleUtil.getStyleSheet(html);
		String targetStyleSheet = null;
		for (String styleSheet : styleSheets) {
			if (styleSheet.contains("Font Definitions")) {
				targetStyleSheet = styleSheet;
			}
		}

		info.description = (targetStyleSheet + Tools.regFind(html, "<div class=\"Section1\">([\\s\\S]*?)<h1>Input</h1>"));
		info.input = (Tools.regFind(html, "<h1>Input</h1>([\\s\\S]*?)<h1>Output</h1>"));
		info.output = (Tools.regFind(html, "<h1>Output</h1>([\\s\\S]*?)<table class=\"Mso\\w+"));
		info.sampleInput = (Tools.regFind(html, "<h1>Output</h1>[\\s\\S]*<table class=\"Mso\\w+[\\s\\S]*?<td[\\s\\S]*?<td[\\s\\S]*?<td[^>]*?>([\\s\\S]*?)</td>"));
		info.sampleOutput = (Tools.regFind(html, "<h1>Output</h1>[\\s\\S]*<table class=\"Mso\\w+[\\s\\S]*?<td[\\s\\S]*?<td[\\s\\S]*?<td[\\s\\S]*?<td[^>]*?>([\\s\\S]*?)</td>"));
		info.hint = (Tools.regFind(html, "Note</h1>([\\s\\S]*?)</div>\\s+</body>"));

		info.source = (Tools.regFind(html, "(<div id=\"problem_setter\">[\\s\\S]*?)</div>\\s*</div>\\s*<span id=\"showNavigation\""));

		Validate.isTrue(!StringUtils.isBlank(info.title));
	}

	@Override
	public String getOjName() {
		return "LightOJ";
	}
	
}
