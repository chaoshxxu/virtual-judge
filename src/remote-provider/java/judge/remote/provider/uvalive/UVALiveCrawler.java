package judge.remote.provider.uvalive;

import judge.executor.ExecutorTaskType;
import judge.executor.Task;
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleHttpResponse;
import judge.remote.RemoteOjInfo;
import judge.remote.crawler.RawProblemInfo;
import judge.remote.crawler.SyncCrawler;
import judge.tool.HtmlHandleUtil;
import judge.tool.Tools;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UVALiveCrawler extends SyncCrawler {

    @Autowired
    private UVALiveProblemIdMapHelper helper;

    @Override
    public RemoteOjInfo getOjInfo() {
        return UVALiveInfo.INFO;
    }

    @Override
    public RawProblemInfo crawl(final String problemId1) throws Exception {
        Validate.isTrue(problemId1.matches("\\d{3,5}"));

        final String problemId2 = helper.getProblemId2(problemId1);
        Validate.isTrue(!StringUtils.isEmpty(problemId2));

        final HttpHost host = new HttpHost("icpcarchive.ecs.baylor.edu", 443, "https");
        final DedicatedHttpClient client = dedicatedHttpClientFactory.build(host);

        final String outerUrl = host.toURI() + "/index.php?option=com_onlinejudge&Itemid=8&page=show_problem&problem=" + problemId2;
        Task<String> taskOuter = new Task<String>(ExecutorTaskType.GENERAL) {
            @Override
            public String call() throws Exception {
                String html = client.get(outerUrl, HttpStatusValidator.SC_OK).getBody();
                return HtmlHandleUtil.transformUrlToAbs(html, outerUrl);
            }
        };

        Task<String> taskInner = new Task<String>(ExecutorTaskType.GENERAL) {
            @Override
            public String call() throws Exception {
                String url = host.toURI() + "/external/" + Integer.parseInt(problemId1) / 100 + "/" + problemId1 + ".html";
                SimpleHttpResponse response = client.get(url);
                if (response.getStatusCode() == HttpStatus.SC_OK) {
                    String html = client.get(url, HttpStatusValidator.SC_OK).getBody();
                    html = HtmlHandleUtil.transformUrlToAbs(html, url);
                    //some problems' description are fucking long, only get the body.innerHTML
                    return html.replaceAll("(?i)^[\\s\\S]*<body[^>]*>", "").replaceAll("(?i)</body>[\\s\\S]*", "");
                } else {
                    return "";
                }
            }
        };

        taskOuter.submit();
        taskInner.submit();
        String htmlOuter = taskOuter.get();
        String htmlInner = taskInner.get();

        RawProblemInfo info = new RawProblemInfo();
        info.title = Tools.regFind(htmlOuter, "<h3>" + problemId1 + " - (.+?)</h3>").trim();
        info.timeLimit = Integer.parseInt(Tools.regFind(htmlOuter, "Time limit: ([\\d\\.]+)").replaceAll("\\.", ""));
        info.memoryLimit = 0;
        info.source = Tools.regFind(htmlOuter, "<br />\\s*(.+)<br />\\s*<h3>" + problemId1).trim();
        info.url = outerUrl;

        String descriptionPrefix =
                "<style type=\"text/css\">h1,h2,h3,h4,h5,h6{margin-bottom:0;}div.textBG p{margin: 0 0 0.0001pt;}</style>" +
                        "<span style='float:right'><a target='_blank' href='https://icpcarchive.ecs.baylor.edu/external/" + Integer.parseInt(problemId1) / 100 + "/" + problemId1 + ".pdf'><img width='100' height='26' border='0' title='Download as PDF' alt='Download as PDF' src='https://icpcarchive.ecs.baylor.edu/components/com_onlinejudge/images/button_pdf.png'></a></span><div style='clear:both'></div>";
        String sampleInput = Tools.regFind(htmlInner, "Sample Int?put</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*?)<H2><FONT size=\"4\" COLOR=\"#ff0000\"><A NAME=\"SECTION000100\\d000000000000000\"");
        String sampleOutput = Tools.regFind(htmlInner, "Sample Output</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*)");
        if (StringUtils.isEmpty(sampleInput) || StringUtils.isEmpty(sampleOutput)) {
            info.description = descriptionPrefix + htmlInner;
        } else {
            info.description = descriptionPrefix + Tools.regFind(htmlInner, "^([\\s\\S]*?)<H2><FONT size=\"4\" COLOR=\"#ff0000\"><A NAME=\"SECTION000100\\d000000000000000\"");
            info.input = Tools.regFind(htmlInner, "Int?put</A>&nbsp;</FONT></H2>([\\s\\S]*?)<H2><FONT size=\"4\" COLOR=\"#ff0000\"><A NAME=\"SECTION000100\\d000000000000000\"");
            info.output = Tools.regFind(htmlInner, "Output</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*?)<H2><FONT size=\"4\" COLOR=\"#ff0000\"><A NAME=\"SECTION000100\\d000000000000000\"");
            info.sampleInput = sampleInput;
            info.sampleOutput = sampleOutput;
        }

        return info;
    }
}
