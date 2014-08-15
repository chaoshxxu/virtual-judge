package judge.remote.crawler;

import judge.remote.crawler.common.RawProblemInfo;
import judge.remote.crawler.common.SimpleCrawler;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;

public class URALCrawler extends SimpleCrawler {

	@Override
	public String getOjName() {
		return "URAL";
	}

	@Override
	protected HttpHost getHost() {
		return new HttpHost("acm.timus.ru");
	}

	@Override
	protected String getProblemUrl(String problemId) {
		return getHost().toURI() + "/print.aspx?space=1&num=" + problemId;
	}
	
	@Override
	protected void preValidate(String problemId) {
		Validate.isTrue(problemId.matches("[1-9]\\d*"));
	}

	@Override
	protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
		info.title = Tools.regFind(html, "problem_title\">\\d{4}. ([\\s\\S]*?)</H2>").trim();
		info.timeLimit = ((int)(1000 * Double.parseDouble(Tools.regFind(html, "Time Limit: ([\\d\\.]*?) second"))));
		info.memoryLimit = (1024 * Integer.parseInt(Tools.regFind(html, "Memory Limit: ([\\d\\.]*?) MB")));
		info.description = (Tools.regFind(html, "problem_text\">([\\s\\S]*?)<H3 CLASS=\"problem_subtitle\">Input"));
		info.input = (Tools.regFind(html, "problem_subtitle\">Input</H3>([\\s\\S]*?)<H3 CLASS=\"problem_subtitle\">Output"));
		info.output = (Tools.regFind(html, "problem_subtitle\">Output</H3>([\\s\\S]*?)<H3 CLASS=\"problem_subtitle\">Sample"));
		info.sampleInput = 
				"<style type=\"text/css\">TABLE.sample{border-collapse:collapse;border: solid 1px #1A5CC8;}TABLE.sample TR TD, TABLE.sample TR TH{border: solid 1px #1A5CC8;vertical-align: top;padding: 3px;}TABLE.sample TR TH{color: #1A5CC8;}</style>"
				+ Tools.regFind(html, "problem_subtitle\">Samples*</H3>([\\s\\S]*?)(<DIV CLASS=\"problem_source\">|<H3 CLASS=\"problem_subtitle\">Hint)");
		info.hint = (Tools.regFind(html, "problem_subtitle\">Hint</H3>([\\s\\S]*?)<DIV CLASS=\"problem_source"));
		info.source = (Tools.regFind(html, "<DIV CLASS=\"problem_source\">([\\s\\S]*?)</DIV></DIV>"));
		info.url = info.url.replace("/print", "/problem");
	}

}
