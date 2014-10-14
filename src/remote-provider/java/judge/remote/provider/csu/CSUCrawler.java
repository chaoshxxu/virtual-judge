package judge.remote.provider.csu;

import judge.remote.RemoteOjInfo;
import judge.remote.crawler.RawProblemInfo;
import judge.remote.crawler.SimpleCrawler;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class CSUCrawler extends SimpleCrawler {

    @Override
    public RemoteOjInfo getOjInfo() {
        return CSUInfo.INFO;
    }

    @Override
    protected String getProblemUrl(String problemId) {
        return getHost().toURI() + "/OnlineJudge/problem.php?id=" + problemId;
    }
    
    @Override
    protected void preValidate(String problemId) {
        Validate.isTrue(problemId.matches("[1-9]\\d*"));
    }

    @Override
    protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
        info.title = Tools.regFind(html, "<center><h2>\\d+:([\\s\\S]*?)</h2>").trim();
        info.timeLimit = (1000 * Integer.parseInt(Tools.regFind(html, "Time Limit: </span>(\\d+) Sec")));
        info.memoryLimit = (1024 * Integer.parseInt(Tools.regFind(html, "Memory Limit: </span>(\\d+) MB")));
        info.description = (Tools.regFind(html, "<h2>Description</h2>([\\s\\S]*?)<h2>Input</h2>"));
        info.input = (Tools.regFind(html, "<h2>Input</h2>([\\s\\S]*?)<h2>Output</h2>"));
        info.output = (Tools.regFind(html, "<h2>Output</h2>([\\s\\S]*?)<h2>Sample Input</h2>"));
        info.sampleInput = (Tools.regFind(html, "<h2>Sample Input</h2>([\\s\\S]*?)<h2>Sample Output</h2>").replaceAll("<span", "<pre").replaceAll("</span>", "</pre>"));
        info.sampleOutput = (Tools.regFind(html, "<h2>Sample Output</h2>([\\s\\S]*?)<h2>HINT</h2>").replaceAll("<span", "<pre").replaceAll("</span>", "</pre>"));
        info.hint = (Tools.regFind(html, "<h2>HINT</h2>([\\s\\S]*?)<h2>Source</h2>"));
        info.source = (Tools.regFind(html, "<h2>Source</h2>[\\s\\S]*?<div class=\"content\"><p>([\\s\\S]*?)</p></div><center>"));
    }

}
