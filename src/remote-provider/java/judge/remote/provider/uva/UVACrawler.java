package judge.remote.provider.uva;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.executor.CascadeTask;
import judge.executor.ExecutorTaskType;
import judge.executor.Task;
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.DedicatedHttpClientFactory;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleHttpResponse;
import judge.remote.RemoteOj;
import judge.remote.crawler.RawProblemInfo;
import judge.remote.crawler.SyncCrawler;
import judge.remote.provider.uva.UVaProblemIdCrawlTask.UVaProblemInfo;
import judge.tool.HtmlHandleUtil;
import judge.tool.SpringBean;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UVACrawler extends SyncCrawler {
    
    private static UVaProblemIdMapHelper helper = new UVaProblemIdMapHelper();

    @Override
    public RemoteOj getOj() {
        return RemoteOj.UVA;
    }

    @Override
    public RawProblemInfo crawl(final String problemId1) throws Exception {
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

/**
 * <pre>
 * UVa has two standards of "problem id".
 * For example, to access the problem "100 - The 3n + 1 problem", you need to know another magic number other than "100", it's "36".
 * 
 * The URL pointing to the page containing problem title and time limit is:
 * http://uva.onlinejudge.org/index.php?option=com_onlinejudge&Itemid=8&category=3&page=show_problem&problem=36
 * 
 * The URL pointing to the page containing description is:
 * http://uva.onlinejudge.org/external/1/100.html
 * 
 * This class helps to get "36" given "100".
 * To distinguish them, I'd like to call "100" problem_id_1, and call "36" problem_id_2.
 * </pre>
 * 
 * @author Isun
 * 
 */
class UVaProblemIdMapHelper {
    private final static Logger log = LoggerFactory.getLogger(UVaProblemIdMapHelper.class);

    /**
     * category -> source
     */
    private Map<String, String> categoryInfo = new ConcurrentHashMap<String, String>();
    private Map<String, UVaProblemInfo> problemInfo = new ConcurrentHashMap<String, UVaProblemInfo>();
    private long lastUpdateTime;
    
    public UVaProblemInfo getProblemInfo(String problemId1) throws InterruptedException, ExecutionException {
        if (!problemInfo.containsKey(problemId1)) {
            refresh();
        }
        return problemInfo.get(problemId1);
    }
    
    public String getSource(String category) throws InterruptedException, ExecutionException {
        if (!categoryInfo.containsKey(category)) {
            refresh();
        }
        return categoryInfo.get(category);
    }
    
    public synchronized void refresh() throws InterruptedException, ExecutionException {
        if (System.currentTimeMillis() - lastUpdateTime < 3600000L) {
            return;
        }
        lastUpdateTime = System.currentTimeMillis();
        
        long begin = System.currentTimeMillis();
        UVaProblemIdCrawlTask task = new UVaProblemIdCrawlTask("0", problemInfo, categoryInfo);
        task.get();
        log.info("UVa problem id map init cost " + (System.currentTimeMillis() - begin) + "ms");
    }
}

class UVaProblemIdCrawlTask extends CascadeTask<Void> {
    private final static Logger log = LoggerFactory.getLogger(UVaProblemIdCrawlTask.class);

    private String category;
    private Map<String, UVaProblemInfo> problemInfo;
    private Map<String, String> categoryInfo;

    public UVaProblemIdCrawlTask(
            String category, 
            Map<String, UVaProblemInfo> problemInfo, 
            Map<String, String> categoryInfo) {
        super(ExecutorTaskType.INIT_UVA_PROBLEM_ID_MAP);
        this.category = category;
        this.problemInfo = problemInfo;
        this.categoryInfo = categoryInfo;
    }

    @Override
    public Void call() {
        if (categoryInfo.containsKey(category)) {
            return null;
        }
        
        log.info("> UVa problem id mapping, category = " + category);
        
        HttpHost host = new HttpHost("uva.onlinejudge.org");
        DedicatedHttpClient client = SpringBean.getBean(DedicatedHttpClientFactory.class).build(host);
        String listPageUrl = "/index.php?option=com_onlinejudge&Itemid=8&limit=1000&limitstart=0&category=" + category;
        String html = client.get(listPageUrl, HttpStatusValidator.SC_OK).getBody();
        String tranformedHtml = HtmlHandleUtil.transformUrlToAbs(html, host.toURI() + listPageUrl);
        
        // 1. find problem
        boolean foundProblem = false;
        Matcher matcher = Pattern.compile("problem=(\\d+)\">(\\d+)").matcher(html);
        while (matcher.find()) {
            foundProblem = true;
            String problemId1 = matcher.group(2);
            String problemId2 = matcher.group(1);
            UVaProblemInfo info = problemInfo.get(problemId1);
            if (info == null) {
                info = new UVaProblemInfo();
                problemInfo.put(problemId1, info);
            }
            info.problemId1 = problemId1;
            info.problemId2 = problemId2;
            info.categories.add(category + "");
        }
        
        // 2. record the source
        categoryInfo.put(category, "");
        if (foundProblem) {
            String source = Tools.regFind(tranformedHtml, "contentheading[\\s\\S]*?(<a[\\s\\S]+?)</div>");
            if (!source.contains("Problem Set Volumes") && !source.contains("Contest Volumes")) {
                // Only remain the last link
                source = source.replaceAll("<a.*?>(.+?)</a>(?=[\\s\\S]*<)", "$1");
                categoryInfo.put(category, source);
            }
        }
        
        // 3. find sub-category
        matcher = Pattern.compile("category=(\\d+)").matcher(html);
        while (matcher.find()) {
            String newCategory = matcher.group(1);
            UVaProblemIdCrawlTask childTask = new UVaProblemIdCrawlTask(newCategory, problemInfo, categoryInfo);
            addChildTask(childTask);
        }
        log.info("< UVa problem id mapping, category = " + category);
        return null;
    }
    
    class UVaProblemInfo {
        String problemId1;
        String problemId2;
        HashSet<String> categories = new HashSet<String>();
    }
    
}