package judge.remote.provider.hdu;

import judge.remote.RemoteOj;
import judge.remote.crawler.RawProblemInfo;
import judge.remote.crawler.SimpleCrawler;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class HDUCrawler extends SimpleCrawler {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.HDU;
    }

    @Override
    protected String getProblemUrl(String problemId) {
        return getHost().toURI() + "/showproblem.php?pid=" + problemId;
    }
    
    @Override
    protected void preValidate(String problemId) {
        Validate.isTrue(problemId.matches("[1-9]\\d*"));
    }

    @Override
    protected void populateProblemInfo(RawProblemInfo info, String problemId, String html) {
        info.title = Tools.regFind(html, "color:#1A5CC8\">([\\s\\S]*?)</h1>").trim();
        info.timeLimit = (Integer.parseInt(Tools.regFind(html, "(\\d*) MS")));
        info.memoryLimit = (Integer.parseInt(Tools.regFind(html, "/(\\d*) K")));
        info.description = (Tools.regFind(html, "> Problem Description </div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
        info.input = (Tools.regFind(html, "> Input </div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
        info.output = (Tools.regFind(html, "> Output </div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
        info.sampleInput = (Tools.regFind(html, "> Sample Input </div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
        info.sampleOutput = (Tools.regFind(html, "> Sample Output </div>([\\s\\S]*?)(<br /><[^<>]*?panel_title[^<>]*?>|<[^<>]*?><[^<>]*?><i>Hint)") + "</div></div>");
        info.hint = (Tools.regFind(html, "<i>Hint</i></div>([\\s\\S]*?)<br /><[^<>]*?panel_title[^<>]*?>"));
        if (!StringUtils.isEmpty(info.hint)){
            info.hint = "<pre>" + info.hint + "</pre>";
        }
        info.source = (Tools.regFind(html, "Source </div><div class=\"panel_content\">([\\s\\S]*?)<[^<>]*?panel_[^<>]*?>").replaceAll("<[\\s\\S]*?>", ""));
    }

}
