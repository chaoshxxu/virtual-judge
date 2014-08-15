package judge.remote.crawler;

import judge.remote.crawler.common.RawProblemInfo;
import judge.remote.crawler.common.SimpleCrawler;
import judge.tool.Tools;

import org.apache.http.HttpHost;

public class AizuCrawler extends SimpleCrawler {

	@Override
	public String getOjName() {
		return "Aizu";
	}
	
	@Override
	protected HttpHost getHost() {
		return new HttpHost("judge.u-aizu.ac.jp");
	}

	@Override
	protected String getProblemUrl(String problemId) {
		return getHost().toURI() + "/onlinejudge/description.jsp?id=" + problemId;
	}

	@Override
	protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
		info.title = Tools.regFind(html, "<h1 class=\"title\">([\\s\\S]*?)</h1>").trim();
		info.timeLimit = 1000 * Integer.parseInt(Tools.regFind(html, "Time Limit : (\\d+) sec"));
		info.memoryLimit = Integer.parseInt(Tools.regFind(html, "Memory Limit : (\\d+) KB"));
		info.description = Tools.regFind(html, "<div class=\"description\">([\\s\\S]*?)<hr />").replaceAll("^[\\s\\S]*</h1>", "");
		info.source = Tools.regFind(html, "style=\"font-size:10pt\">\\s*Source:([\\s\\S]*?)</div>");
	}

}
