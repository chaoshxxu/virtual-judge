package judge.remote.crawler;

import judge.remote.RemoteOj;
import judge.remote.crawler.common.RawProblemInfo;
import judge.remote.crawler.common.SimpleCrawler;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class CodeForcesCrawler extends SimpleCrawler {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.CodeForces;
    }

    @Override
    protected String getProblemUrl(String problemId) {
        String contestNum = problemId.replaceAll("\\D.*", "");
        String problemNum = problemId.replaceAll("^\\d*", "");
        return getHost().toURI() + "/problemset/problem/" + contestNum + "/" + problemNum;
    }
    
    @Override
    protected void preValidate(String problemId) {
        Validate.isTrue(problemId.matches("[1-9]\\w*"));
    }

    @Override
    protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
        String problemNum = problemId.replaceAll("^\\d*", "");

        info.title = Tools.regFind(html, "<div class=\"title\">\\s*" + problemNum + "\\. ([\\s\\S]*?)</div>").trim();
        Double timeLimit = 1000 * Double.parseDouble(Tools.regFind(html, "</div>([\\d\\.]+) seconds?\\s*</div>"));
        info.timeLimit = (timeLimit.intValue());
        info.memoryLimit = (1024 * Integer.parseInt(Tools.regFind(html, "</div>(\\d+) megabytes\\s*</div>")));
        info.description = (Tools.regFind(html, "standard output\\s*</div></div><div>([\\s\\S]*?)</div><div class=\"input-specification"));
        if (StringUtils.isEmpty(info.description)) {
            info.description = ("<div>" + Tools.regFind(html, "(<div class=\"input-file\">[\\s\\S]*?)</div><div class=\"input-specification"));
        }
        info.input = (Tools.regFind(html, "<div class=\"section-title\">\\s*Input\\s*</div>([\\s\\S]*?)</div><div class=\"output-specification\">"));
        info.output = (Tools.regFind(html, "<div class=\"section-title\">\\s*Output\\s*</div>([\\s\\S]*?)</div><div class=\"sample-tests\">"));
        info.sampleInput = ("<style type=\"text/css\">.input, .output {border: 1px solid #888888;} .output {margin-bottom:1em;position:relative;top:-1px;} .output pre,.input pre {background-color:#EFEFEF;line-height:1.25em;margin:0;padding:0.25em;} .title {background-color:#FFFFFF;border-bottom: 1px solid #888888;font-family:arial;font-weight:bold;padding:0.25em;}</style>" + Tools.regFind(html, "<div class=\"sample-test\">([\\s\\S]*?)</div>\\s*</div>\\s*</div>"));
        info.hint = (Tools.regFind(html, "<div class=\"section-title\">\\s*Note\\s*</div>([\\s\\S]*?)</div></div></div></div>"));
    }

}
