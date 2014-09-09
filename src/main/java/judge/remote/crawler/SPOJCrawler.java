package judge.remote.crawler;

import judge.remote.RemoteOj;
import judge.remote.crawler.common.RawProblemInfo;
import judge.remote.crawler.common.SimpleCrawler;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class SPOJCrawler extends SimpleCrawler {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.SPOJ;
	}

	@Override
	protected String getProblemUrl(String problemId) {
		return getHost().toURI() + "/problems/" + problemId;
	}
	
	@Override
	protected void preValidate(String problemId) {
		Validate.isTrue(problemId.matches("\\S+"));
	}

	@Override
	protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
		Validate.isTrue(!html.contains("Wrong problem code!"));
		Validate.isTrue(html.contains("<h2>SPOJ Problem Set (classical)</h2>") || html.contains("<h2>SPOJ Problem Set (tutorial)</h2>"));

		info.title = Tools.regFind(html, "<h1>\\d+\\.([\\s\\S]*?)</h1>").trim();
		info.timeLimit = (int) (1000 * Double.parseDouble(Tools.regFind(html, "Time limit:</td><td>([\\s\\S]*?)s")));
		info.description = (Tools.regFind(html, "<p align=\"justify\"></p>([\\s\\S]*?)<hr /><table border=\"0\""));
		info.source = (Tools.regFind(html, "Resource:</td><td>([\\s\\S]*?)</td></tr>"));
	}

}
