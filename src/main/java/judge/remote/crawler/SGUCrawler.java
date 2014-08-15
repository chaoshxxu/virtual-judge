package judge.remote.crawler;

import judge.remote.crawler.common.RawProblemInfo;
import judge.remote.crawler.common.SimpleCrawler;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;

public class SGUCrawler extends SimpleCrawler {

	@Override
	public String getOjName() {
		return "SGU";
	}

	@Override
	protected HttpHost getHost() {
		return new HttpHost("acm.sgu.ru");
	}

	@Override
	protected String getProblemUrl(String problemId) {
		return getHost().toURI() + "/problem.php?contest=0&problem=" + problemId;
	}

	@Override
	protected void preValidate(String problemId) {
		Validate.isTrue(problemId.matches("[1-9]\\d*"));
	}

	@Override
	protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
		String timeLimitStr = Tools.regFind(html, "ime limit( per test)?: ([\\d\\.]*)", 2);
		info.timeLimit = (int)(1000 * Double.parseDouble(timeLimitStr));
		
		String memoryLimitStr = Tools.regFind(html, "emory limit( per test)?: ([\\d]*)", 2);
		info.memoryLimit = (int) Double.parseDouble(memoryLimitStr);
		
		info.title = Tools.regFind(html, problemId + "\\.([\\s\\S]*?)</[th]", 1).trim();
		if (html.contains("<title> SSU Online Contester")) {
			info.description = (Tools.regFind(html, "output: standard </div><br />([\\s\\S]*?)<br /><br /><div align=\"left\" style=\"margin-top:1em;\"><b>Input</b>"));
			info.input = (Tools.regFind(html, "<b>Input</b></div>([\\s\\S]*?)<br /><br /><div align=\"left\" style=\"margin-top:1em;\"><b>Output</b>"));
			info.output = (Tools.regFind(html, "<b>Output</b></div>([\\s\\S]*?)<br /><br /><div align=\"left\" style=\"margin-top:1em;\"><b>Example\\(s\\)</b>"));
			info.sampleInput = (Tools.regFind(html, "<b>Example\\(s\\)</b></div>([\\s\\S]*?)<br /><br />(<div align=\"left\" style=\"margin-top:1em;\"><b>Note</b>|</div><hr />)"));
			info.hint = (Tools.regFind(html, "<b>Note</b></div>([\\s\\S]*?)<br /><br /></div><hr />"));
		} else if (html.contains("<title>Saratov State University")) {
			info.description = (Tools.regFind(html, "output:\\s*standard[\\s\\S]*?</div><br><br><br>([\\s\\S]*?)<div align = left><br><b>Input</b>"));
			info.input = (Tools.regFind(html, "<b>Input</b></div>([\\s\\S]*?)<div align = left><br><b>Output</b>"));
			info.input = (Tools.regFind(html, "<b>Output</b></div>([\\s\\S]*?)<div align = left><br><b>Sample test"));
			info.sampleInput = (Tools.regFind(html, "<b>Sample test\\(s\\)</b></div>([\\s\\S]*?)(<div align = left><br><b>Note</b>|<div align = right>)"));
			info.hint = (Tools.regFind(html, "<b>Note</b></div>([\\s\\S]*?)<div align = right>"));
		}
		if (StringUtils.isEmpty(info.input) || StringUtils.isEmpty(info.output)) {
			info.description = (html.replaceAll("(?i)[\\s\\S]*\\d{3,}\\s*KB\\s*</P>", "").replaceAll("(?i)</?(body|html)>", ""));
		}
		info.source = (Tools.regFind(html, "Resource:</td><td>([\\s\\S]*?)\n</td>"));
	}

}
