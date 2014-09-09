package judge.remote.crawler;

import judge.remote.RemoteOj;
import judge.remote.crawler.common.RawProblemInfo;
import judge.remote.crawler.common.SimpleCrawler;
import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class FZUCrawler extends SimpleCrawler {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.FZU;
	}

	@Override
	protected String getProblemUrl(String problemId) {
		return getHost().toURI() + "/problem.php?pid=" + problemId;
	}
	
	@Override
	protected void preValidate(String problemId) {
		Validate.isTrue(problemId.matches("[1-9]\\d*"));
	}

	@Override
	protected boolean autoTransformAbsoluteHref() {
		return false;
	}

	@Override
	protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
		html = html.replaceAll("<div class\\s*=\\s*\"data\">([\\s\\S]*?)</?div>", "<pre>#####$1#####</pre>");
		html = HtmlHandleUtil.transformUrlToAbs(html, info.url);
		html = html.replaceAll("<pre>#####([\\s\\S]*?)#####</pre>", "<div class=\"data\">$1</div>");
		
		String style = "<style type=\"text/css\">.data {text-align:left;font-family: \"Courier New\", Courier, monospace;font-size: 16px;white-space: pre;line-height:20px;	text-indent: 0px;}</style>";

		info.title = Tools.regFind(html, "<b> Problem \\d+ (.+?)</b>").trim();
		info.timeLimit = (Integer.parseInt(Tools.regFind(html, "Time Limit: (\\d+) mSec")));
		info.memoryLimit = (Integer.parseInt(Tools.regFind(html, "Memory Limit : (\\d+) KB")));
		info.description = style + (Tools.regFind(html, "Problem Description</h2><div class=\"pro_desc\">([\\s\\S]+?)</div>\\s*<h2><img"));
		info.input = (Tools.regFind(html, "Input</h2><div class=\"pro_desc\">([\\s\\S]+?)</div>\\s*<h2><img"));
		info.output = (Tools.regFind(html, "Output</h2><div class=\"pro_desc\">([\\s\\S]+?)</div>\\s*<h2><img"));
		info.sampleInput = (Tools.regFind(html, "Sample Input</h2>([\\s\\S]*?)<h2><img") + "</pre>");
		info.sampleOutput = (Tools.regFind(html, "Sample Output</h2>([\\s\\S]*?)(<h2><img|</div>\\s*<br />)") + "</pre>");
		info.hint = (Tools.regFind(html, "Hint</h2>([\\s\\S]+?)(<h2><img|</div>\\s*<br />)"));
		info.source = (Tools.regFind(html, "Source</h2>([\\s\\S]+?)(<h2><img|</div>\\s*<br />)"));
	}

}
