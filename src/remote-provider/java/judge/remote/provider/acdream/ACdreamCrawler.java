package judge.remote.provider.acdream;

import judge.remote.RemoteOjInfo;
import judge.remote.crawler.RawProblemInfo;
import judge.remote.crawler.SimpleCrawler;
import judge.tool.Tools;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class ACdreamCrawler extends SimpleCrawler {

    @Override
    public RemoteOjInfo getOjInfo() {
        return ACdreamInfo.INFO;
    }

    @Override
    protected String getProblemUrl(String problemId) {
        return getHost().toURI() + "/problem?pid=" + problemId;
    }

    @Override
    protected void preValidate(String problemId) {
        Validate.isTrue(problemId.matches("[1-9]\\d{3,}"));
    }

    @Override
    protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
        info.title = Tools.regFind(html, "<h3 class=\"problem-header\">(.+?)</h3>").trim();
        info.timeLimit = Integer.parseInt(Tools.regFind(html, "(\\d+)MS \\(Java/Others\\)"));
        info.memoryLimit = Integer.parseInt(Tools.regFind(html, "(\\d+)KB \\(Java/Others\\)"));
        info.description = Tools.regFind(html, "<h4>Problem Description</h4>([\\s\\S]*?)<h4>");
        info.input = Tools.regFind(html, "<h4>Input</h4>([\\s\\S]*?)<h4>");
        info.output = Tools.regFind(html, "<h4>Output</h4>([\\s\\S]*?)<h4>");
        info.sampleInput = Tools.regFind(html, "<h4>Sample Input</h4>([\\s\\S]*?)<h4>");
        info.sampleOutput = Tools.regFind(html, "<h4>Sample Output</h4>([\\s\\S]*?)<h4>");
        info.hint = Tools.regFind(html, "<h4>Hint</h4>([\\s\\S]*?)<h4>");
        info.source = Tools.regFind(html, "<h4>Source</h4>([\\s\\S]*?)<h4>");
    }

}
