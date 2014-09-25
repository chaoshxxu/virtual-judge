package judge.remote.provider.nbut;

import judge.remote.RemoteOj;
import judge.remote.crawler.RawProblemInfo;
import judge.remote.crawler.SimpleCrawler;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class NBUTCrawler extends SimpleCrawler {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.NBUT;
    }

    @Override
    protected String getProblemUrl(String problemId) {
        return getHost().toURI() + "/Problem/view.xhtml?id=" + problemId;
    }
    
    @Override
    protected void preValidate(String problemId) {
        Validate.isTrue(problemId.matches("[1-9]\\d*"));
    }

    @Override
    protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
        info.title = Tools.regFind(html, "\\["+ problemId + "\\]([\\s\\S]*?)</title>").trim();
        info.timeLimit = (Integer.parseInt(Tools.regFind(html, "时间限制: (\\d+)")));
        info.memoryLimit = (Integer.parseInt(Tools.regFind(html, "内存限制: (\\d+)")));
        info.description = (Tools.regFind(html, "<li class=\"contents\" id=\"description\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"input-title\">"));
        info.input = (Tools.regFind(html, "<li class=\"contents\" id=\"input\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"output-title\">"));
        info.output = (Tools.regFind(html, "<li class=\"contents\" id=\"output\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"sampleinput-title\">"));
        info.sampleInput = (Tools.regFind(html, "<li class=\"contents\" id=\"sampleinput\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"sampleoutput-title\">"));
        info.sampleOutput = (Tools.regFind(html, "<li class=\"contents\" id=\"sampleoutput\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"hint-title\">"));
        info.hint = (Tools.regFind(html, "<li class=\"contents\" id=\"hint\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"source-title\">"));
        info.source = (Tools.regFind(html, "<li class=\"contents\" id=\"source\">([\\s\\S]*?)</li>\\s*<li class=\"titles\" id=\"operation-title\">").replaceAll("<pre>([^<>]*)</pre>", "$1"));
    }

}
