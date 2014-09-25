package judge.remote.provider.zoj;

import judge.remote.RemoteOj;
import judge.remote.crawler.RawProblemInfo;
import judge.remote.crawler.SimpleCrawler;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class ZOJCrawler extends SimpleCrawler {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.ZOJ;
    }

    @Override
    protected String getProblemUrl(String problemId) {
        return getHost().toURI() + "/onlinejudge/showProblem.do?problemCode=" + problemId;
    }
    
    @Override
    protected void preValidate(String problemId) {
        Validate.isTrue(problemId.matches("[1-9]\\d*"));
    }

    @Override
    protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
        info.title = Tools.regFind(html, "<span class=\"bigProblemTitle\">([\\s\\S]*?)</span>").trim();
        info.timeLimit = (1000 * Integer.parseInt(Tools.regFind(html, "Time Limit: </font> ([\\s\\S]*?) Second")));
        info.memoryLimit = (Integer.parseInt(Tools.regFind(html, "Memory Limit: </font> ([\\s\\S]*?) KB")));
        if (html.contains(">Input<") && html.contains(">Output<") && html.contains(">Sample Input<") && html.contains(">Sample Output<")){
            info.description = (Tools.regFind(html, "KB[\\s\\S]*?</center><hr />([\\s\\S]*?)>[\\s]*Input"));
            info.input = (Tools.regFindCaseSensitive(html, ">[\\s]*Input([\\s\\S]*?)>[\\s]*Out?put"));
            info.output = (Tools.regFindCaseSensitive(html, ">[\\s]*Out?put([\\s\\S]*?)>[\\s]*Sample Input"));
            info.sampleInput = (Tools.regFind(html, ">[\\s]*Sample Input([\\s\\S]*?)>[\\s]*Sample Out?put"));
            info.sampleOutput = (Tools.regFind(html, ">[\\s]*Sample Out?put([\\s\\S]*?)<hr"));
        } else {
            info.description = (Tools.regFind(html, "KB[\\s\\S]*?</center><hr />([\\s\\S]*?)<hr />"));
        }
        info.source = (Tools.regFind(html, "Source:\\s*<strong>([\\s\\S]*?)</strong><br />"));
    }

}
