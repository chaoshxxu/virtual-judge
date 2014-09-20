package judge.bean;

import java.util.Date;
import java.util.Set;

/**
 * 题目
 * @author Isun
 *
 */
public class Problem {
    private int id;            //Hibernate统编ID
    private String title;        //标题
    private String source;        //出处
    private String url;            //题面原始url
    private String originOJ;    //原始OJ
    private String originProb;    //原始OJ题号
    private int memoryLimit;    //内存限制(KB)
    private int timeLimit;        //时间限制(ms)(1:crawling 2:crawl failed)

    private Date triggerTime;    //上次尝试更新题目描述时间

    private Set<Description> descriptions;
    private Set<Cproblem> cproblems;
    private Set<Submission> submissions;


    public Problem(int id){
        this.id = id;
    }

    public Problem(){}


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        if (source != null){
            source = source.trim();
            if (source.matches("(<[^<>]*>\\s*)*")){
                source = "";
            }
        }
        this.source = source;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getOriginOJ() {
        return originOJ;
    }
    public void setOriginOJ(String originOJ) {
        this.originOJ = originOJ;
    }
    public String getOriginProb() {
        return originProb;
    }
    public void setOriginProb(String originProb) {
        this.originProb = originProb;
    }
    public int getMemoryLimit() {
        return memoryLimit;
    }
    public void setMemoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
    }
    public int getTimeLimit() {
        return timeLimit;
    }
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
    public Date getTriggerTime() {
        return triggerTime;
    }
    public void setTriggerTime(Date triggerTime) {
        this.triggerTime = triggerTime;
    }
    public Set<Description> getDescriptions() {
        return descriptions;
    }
    public void setDescriptions(Set<Description> descriptions) {
        this.descriptions = descriptions;
    }
    public Set<Cproblem> getCproblems() {
        return cproblems;
    }
    public void setCproblems(Set<Cproblem> cproblems) {
        this.cproblems = cproblems;
    }
    public Set<Submission> getSubmissions() {
        return submissions;
    }
    public void setSubmissions(Set<Submission> submissions) {
        this.submissions = submissions;
    }

}
