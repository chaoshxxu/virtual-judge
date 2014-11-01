package judge.remote.provider.uva;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class UVACrawler extends SyncCrawler {

    @Autowired
    private UVaProblemIdMapHelper helper;

    @Override
    public RemoteOjInfo getOjInfo() {
        return UVAInfo.INFO;
    }

    @Override
    public RawProblemInfo crawl(final String problemId1) throws Exception {
        Validate.isTrue(problemId1.matches("\\d{3,5}"));

        final UVaProblemInfo uvaProblemInfo = helper.getProblemInfo(problemId1);
        final HttpHost host = new HttpHost("uva.onlinejudge.org");
        final String outerUrl = host.toURI() + "/index.php?option=com_onlinejudge&Itemid=8&page=show_problem&problem=" + uvaProblemInfo.problemId2;
        Task<String> taskOuter = new Task<String>(ExecutorTaskType.GENERAL) {
            @Override
            public String call() {
                DedicatedHttpClient client = dedicatedHttpClientFactory.build(host);
                String html = client.get(outerUrl, HttpStatusValidator.SC_OK).getBody();
                return HtmlHandleUtil.transformUrlToAbs(html, outerUrl);
            }
        };

        Task<String> taskInner = new Task<String>(ExecutorTaskType.GENERAL) {
            @Override
            public String call() {
                DedicatedHttpClient client = dedicatedHttpClientFactory.build(host, "Windows-1252");
                String url = host.toURI() + "/external/" + Integer.parseInt(problemId1) / 100 + "/" + problemId1 + ".html";
                SimpleHttpResponse response = client.get(url);
                if (response.getStatusCode() == HttpStatus.SC_OK) {
                    String html = response.getBody();
                    return HtmlHandleUtil.transformUrlToAbs(html, url);
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
        info.source = getSource(uvaProblemInfo);
        info.url = outerUrl;

        String descriptionPrefix =
                "<style type=\"text/css\">h1,h2,h3,h4,h5,h6{margin-bottom:0;}div.textBG p{margin: 0 0 0.0001pt;}</style>" +
                        "<span style='float:right'><a target='_blank' href='http://uva.onlinejudge.org/external/" + Integer.parseInt(problemId1) / 100 + "/" + problemId1 + ".pdf'><img width='100' height='26' border='0' title='Download as PDF' alt='Download as PDF' src='http://uva.onlinejudge.org/components/com_onlinejudge/images/button_pdf.png'></a></span><div style='clear:both'></div>";
        if (htmlInner.contains("http-equiv=\"Refresh\"")) {
            info.description = descriptionPrefix;
        } else {
            info.description = descriptionPrefix + htmlInner;
        }

        return info;
    }

    private String getSource(UVaProblemInfo uvaProblemInfo) throws InterruptedException, ExecutionException {
        List<String> sources = new ArrayList<String>();
        for (String category : uvaProblemInfo.categories) {
            String source = helper.getSource(category);
            if (source != null) {
                sources.add(source);
            }
        }
        return StringUtils.join(sources, "<br />");
    }

}

