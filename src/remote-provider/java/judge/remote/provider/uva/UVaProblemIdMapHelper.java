package judge.remote.provider.uva;

import com.google.gson.reflect.TypeToken;
import judge.executor.CascadeTask;
import judge.executor.ExecutorTaskType;
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.DedicatedHttpClientFactory;
import judge.httpclient.HttpStatusValidator;
import judge.service.JedisService;
import judge.tool.HtmlHandleUtil;
import judge.tool.SpringBean;
import judge.tool.Tools;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
@Component
public class UVaProblemIdMapHelper {
    private final static Logger log = LoggerFactory.getLogger(UVaProblemIdMapHelper.class);

    private static final String categoryInfo_REDIS_KEY = "vjudge:UVaProblemIdMapHelper:categoryInfo";
    private static final String problemInfo_REDIS_KEY = "vjudge:UVaProblemIdMapHelper:problemInfo";
    private static final String lastUpdateTime_REDIS_KEY = "vjudge:UVaProblemIdMapHelper:lastUpdateTime";

    @Autowired
    private JedisService jedisService;

    /**
     * category -> source
     */
    private Map<String, String> categoryInfo;
    private Map<String, UVaProblemInfo> problemInfo;
    private long lastUpdateTime;

    @PostConstruct
    public void init() {
        try {
            categoryInfo = jedisService.get(categoryInfo_REDIS_KEY, new TypeToken<ConcurrentHashMap<String, String>>() {}.getType());
            problemInfo = jedisService.get(problemInfo_REDIS_KEY, new TypeToken<ConcurrentHashMap<String, UVaProblemInfo>>() {}.getType());
            lastUpdateTime = jedisService.get(lastUpdateTime_REDIS_KEY, Long.class);
        } catch (Throwable t) {
            categoryInfo = new ConcurrentHashMap<>();
            problemInfo = new ConcurrentHashMap<>();
            lastUpdateTime = 0L;
        }
        log.info("categorInfo.size = {}", categoryInfo.size());
        log.info("problemInfo.size = {}", problemInfo.size());
        log.info("lastUpdateTime = {}", lastUpdateTime);
    }

    @PreDestroy
    public void destroy() {
        jedisService.set(categoryInfo_REDIS_KEY, categoryInfo);
        jedisService.set(problemInfo_REDIS_KEY, problemInfo);
        jedisService.set(lastUpdateTime_REDIS_KEY, lastUpdateTime);
        log.info("UVaProblemIdMapHelper is persisted successfully!");
    }

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

class UVaProblemInfo {
    String problemId1;
    String problemId2;
    HashSet<String> categories = new HashSet<String>();
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

}
