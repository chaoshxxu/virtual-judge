package judge.action;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.tools.Tool;

import judge.bean.Cproblem;
import judge.bean.DataTablesPage;
import judge.bean.Description;
import judge.bean.Problem;
import judge.bean.Submission;
import judge.bean.User;
import judge.remote.ProblemInfoUpdateManager;
import judge.remote.RunningSubmissions;
import judge.remote.SubmitCodeManager;
import judge.remote.language.LanguageManager;
import judge.remote.status.RemoteStatusType;
import judge.tool.OnlineTool;
import judge.tool.Tools;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;

/**
 * 处理 题库/练习 前端相关功能
 * @author Isun
 *
 */
@SuppressWarnings("unchecked")
public class ProblemAction extends BaseAction{
    private final static Logger log = LoggerFactory.getLogger(ProblemAction.class);

    private static final long serialVersionUID = 5557740709776919006L;
    private int id;    //problemId
    private int uid;
    private int isOpen;
    private int res;    //result
    private String OJId;
    private String probNum;
    private String title;
    private Problem problem;
    private Description description;
    private Submission submission;
    private List dataList;
    private String language;
    private String source;
    private String redir;
    private String un;
    private String _64Format;
    private Integer isSup;
    private DataTablesPage dataTablesPage;
    private Map<String, String> languageList;
    private String submissionInfo;

    @Autowired
    private ProblemInfoUpdateManager problemInfoUpdateManager;

    @Autowired
    private SubmitCodeManager submitManager;

    @Autowired
    private RunningSubmissions runningSubmissions;

    @Autowired
    private LanguageManager languageManager;

    public String toListProblem() {
        Map session = ActionContext.getContext().getSession();
        if (session.containsKey("error")){
            this.addActionError((String) session.get("error"));
        }
        session.remove("error");
        return SUCCESS;
    }

    public String listProblem() {
        String sortDir = getParameter("order[0][dir]");
        String sortCol = getParameter("order[0][column]");
        String start = getParameter("start");
        String length = getParameter("length");
        String draw = getParameter("draw");

        // (Re)crawl the problem if necessary
        if (OJListLiteral.contains(OJId) && !StringUtils.isBlank(probNum)) {
            problemInfoUpdateManager.updateProblem(OJId, probNum, false);
        }

        Map session = ActionContext.getContext().getSession();
        StringBuffer hql = new StringBuffer(
                "select "
                        + "  problem.originOJ, "
                        + "  problem.originProb, "
                        + "  problem.title, "
                        + "  problem.triggerTime, "
                        + "  problem.source, "
                        + "  problem.id, "
                        + "  problem.id, "
                        + "  problem.timeLimit "
                        + "from "
                        + "  Problem problem "
                        + "where "
                        + "  problem.title != 'N/A' ");
        long cnt = baseService.count(hql.toString());
        dataTablesPage = new DataTablesPage();
        dataTablesPage.setRecordsTotal(cnt);
        if (OJListLiteral.contains(OJId)){
            hql.append(" and problem.originOJ = '" + OJId + "' ");
        }
        Map<String, String> paraMap = new HashMap<String, String>();
        if (!StringUtils.isBlank(probNum)) {
            paraMap.put("probNum", "%" + probNum + "%");
            hql.append(" and problem.originProb like :probNum ");
        }
        if (!StringUtils.isBlank(title)) {
            paraMap.put("title", "%" + title + "%");
            hql.append(" and problem.title like :title ");
        }
        if (!StringUtils.isBlank(source)) {
            paraMap.put("source", "%" + source + "%");
            hql.append(" and problem.source like :source ");
        }
        dataTablesPage.setRecordsFiltered(baseService.count(hql.toString(), paraMap));
        if (sortCol != null){
            if (!"desc".equals(sortDir)) {
                sortDir = "";
            }
            if ("1".equals(sortCol)){
                hql.append(" order by problem.originProb " + sortDir);
            } else if ("2".equals(sortCol)){
                hql.append(" order by problem.title " + sortDir);
            } else if ("3".equals(sortCol)){
                hql.append(" order by problem.triggerTime " + sortDir + ", problem.originProb " + sortDir);
            } else if ("4".equals(sortCol)){
                hql.append(" order by problem.source " + sortDir);
            }
        }
        List<Object[]> data = baseService.list(hql.toString(), paraMap, Integer.parseInt(start), Integer.parseInt(length));
        for (Object[] o : data) {
            o[3] = ((Date)o[3]).getTime();
        }
        dataTablesPage.setData(data);
        dataTablesPage.setDraw(Integer.parseInt(draw));

        if (session.containsKey("error")){
            this.addActionError((String) session.get("error"));
        }
        session.remove("error");

        return SUCCESS;
    }

    public String recrawlProblem() {
        problemInfoUpdateManager.updateProblem(OJId, probNum, true);
        return SUCCESS;
    }

    public String viewProblem(){
        if (!StringUtils.isBlank(OJId) && !StringUtils.isBlank(probNum)) {
            problem = judgeService.findProblem(OJId, probNum);
        } else {
            List<Problem> list = baseService.query("select p from Problem p left join fetch p.descriptions where p.id = " + id);
            problem = list.get(0);
        }
        _64Format = lf.get(problem.getOriginOJ());
        problemInfoUpdateManager.updateProblem(problem, false);
        return SUCCESS;
    }

    public String vote4Description(){
        Map session = ActionContext.getContext().getSession();
        Set votePids = (Set) session.get("votePids");
        if (votePids == null){
            votePids = new HashSet<Integer>();
            session.put("votePids", votePids);
        }
        Description desc = (Description) baseService.query(Description.class, id);
        desc.setVote(desc.getVote() + 1);
        baseService.addOrModify(desc);
        votePids.add(desc.getProblem().getId());
        return SUCCESS;
    }

    public String toSubmit(){
        Map session = ActionContext.getContext().getSession();
        User user = (User) session.get("visitor");
        if (user == null){
            //            session.put("redir", "../problem/toSubmit.action?id=" + id);
            return ERROR;
        }
        ServletContext sc = ServletActionContext.getServletContext();
        problem = (Problem) baseService.query(Problem.class, id);
        if (problem == null) {
            return ERROR;
        }

        languageList = languageManager.getLanguages(problem.getOriginOJ());
        //        languageList = (Map<Object, String>) sc.getAttribute(problem.getOriginOJ());
        isOpen = user.getShare();
        return SUCCESS;
    }


    public String submit() throws Exception{
        Map session = ActionContext.getContext().getSession();
        User user = (User) session.get("visitor");
        if (user == null){
            return ERROR;
        }
        problem = (Problem) baseService.query(Problem.class, id);
        //        ServletContext sc = ServletActionContext.getServletContext();
        //        languageList = (Map<Object, String>) sc.getAttribute(problem.getOriginOJ());
        languageList = languageManager.getLanguages(problem.getOriginOJ());

        if (problem == null){
            this.addActionError("Please submit via normal approach!");
            return INPUT;
        }
        if (problem.getTimeLimit() == 1 || problem.getTimeLimit() == 2){
            this.addActionError("Crawling has not finished!");
            return INPUT;
        }

        if (!languageList.containsKey(language)){
            this.addActionError("No such a language!");
            return INPUT;
        }
        source = new String(Base64.decodeBase64(source), "utf-8");
        if (source.length() < 50){
            this.addActionError("Source code should be longer than 50 characters!");
            return INPUT;
        }
        if (source.getBytes("utf-8").length > 30000){
            this.addActionError("Source code should be shorter than 30000 bytes in UTF-8!");
            return INPUT;
        }
        submission = new Submission();
        submission.setSubTime(new Date());
        submission.setProblem(problem);
        submission.setUser(user);
        submission.setStatus("Pending");
        submission.setStatusCanonical(RemoteStatusType.PENDING.name());
        submission.setLanguage(language);
        submission.setSource(source);
        submission.setIsOpen(isOpen);
        submission.setDispLanguage(languageManager.getLanguages(problem.getOriginOJ()).get(language));
        submission.setUsername(user.getUsername());
        submission.setOriginOJ(problem.getOriginOJ());
        submission.setOriginProb(problem.getOriginProb());
        submission.setLanguageCanonical(Tools.getCanonicalLanguage(submission.getDispLanguage()).toString());
        baseService.addOrModify(submission);
        if (user.getShare() != submission.getIsOpen()) {
            user.setShare(submission.getIsOpen());
            baseService.addOrModify(user);
        }

        submitManager.submitCode(submission);

        return SUCCESS;
    }

    public String status() {
        if (id != 0){
            problem = (Problem) baseService.query(Problem.class, id);
            OJId = problem.getOriginOJ();
            probNum = problem.getOriginProb();
        }
        Map session = ActionContext.getContext().getSession();
        User user = (User) session.get("visitor");
        isSup = user == null ? 0 : user.getSup();

        if (session.containsKey("error")){
            this.addActionError((String) session.get("error"));
        }
        session.remove("error");

        return SUCCESS;
    }

    public String fetchStatus() {
        String start = getParameter("start");
        String length = getParameter("length");
        String draw = getParameter("draw");
        String orderBy = getParameter("orderBy");

        Map session = ActionContext.getContext().getSession();
        Map paraMap = new HashMap();
        User user = (User) session.get("visitor");
        int userId = user != null ? user.getId() : -1;
        int sup = user != null ? user.getSup() : 0;

        StringBuffer hql = new StringBuffer("" +
                "select " +
                "  s.id, " + // 0
                "  s.username, " +
                "  s.problem.id, " +
                "  s.status, " +
                "  s.memory, " +
                "  s.time, " + // 5
                "  s.dispLanguage, " +
                "  length(s.source), " +
                "  s.subTime, " +
                "  s.user.id, " +
                "  s.isOpen, " + // 10
                "  s.originOJ, " +
                "  s.originProb, " +
                "  s.contest.id, " +
                "  s.additionalInfo, " +
                "  s.statusCanonical, " + // 15
                "  s.id " +
                "from " +
                "  Submission s ");

        dataTablesPage = new DataTablesPage();
        dataTablesPage.setRecordsTotal(9999999L);

        if (sup == 0){
            hql.append(" left join s.contest c where (s.isPrivate = 0 and (c is null or c.endTime < :currentTime) or s.user.id = :userId) ");
            paraMap.put("currentTime", new Date());
            paraMap.put("userId", user != null ? user.getId() : -1);
        } else {
            hql.append(" where 1 = 1 ");
        }

        if (un != null && !un.trim().isEmpty()){
            hql.append(" and s.username = :un ");
            paraMap.put("un", un.toLowerCase().trim());
        }

        if (!probNum.isEmpty()){
            hql.append(" and s.originProb = :probNum ");
            paraMap.put("probNum", probNum);
        }
        if (OJListLiteral.contains(OJId)){
            hql.append(" and s.originOJ = :OJId ");
            paraMap.put("OJId", OJId);
        }

        if (res == 1){
            hql.append(" and s.statusCanonical = 'AC' ");
        } else if (res == 2) {
            hql.append(" and s.statusCanonical = 'PE' ");
        } else if (res == 3) {
            hql.append(" and s.statusCanonical = 'WA' ");
        } else if (res == 4) {
            hql.append(" and s.statusCanonical = 'TLE' ");
        } else if (res == 5) {
            hql.append(" and s.statusCanonical = 'MLE' ");
        } else if (res == 6) {
            hql.append(" and s.statusCanonical = 'OLE' ");
        } else if (res == 7) {
            hql.append(" and s.statusCanonical = 'RE' ");
        } else if (res == 8) {
            hql.append(" and s.statusCanonical = 'CE' ");
        } else if (res == 9) {
            hql.append(" and s.statusCanonical in ('FAILED_OTHER', 'SUBMIT_FAILED_PERM') ");
        } else if (res == 10) {
            hql.append(" and s.statusCanonical = 'SUBMIT_FAILED_TEMP' ");
        } else if (res == 11) {
            hql.append(" and s.statusCanonical in ('PENDING', 'SUBMITTED', 'QUEUEING', 'COMPILING', 'JUDGING') ");
        }

        if (!StringUtils.isBlank(language)) {
            hql.append(" and s.languageCanonical = :language ");
            paraMap.put("language", language);
        }

        if (OJListLiteral.contains(OJId) && !StringUtils.isBlank(probNum) && 1 == res) {
            if ("memory".equals(orderBy)) {
                hql.append(" order by s.memory asc, s.time asc, length(s.source) asc ");
            } else if ("time".equals(orderBy)) {
                hql.append(" order by s.time asc, s.memory asc, length(s.source) asc ");
            } else if ("length".equals(orderBy)) {
                hql.append(" order by length(s.source) asc, s.time asc, s.memory asc ");
            } else {
                hql.append(" order by s.id desc ");
            }
        } else {
            hql.append(" order by s.id desc ");
        }

        dataTablesPage.setRecordsFiltered(9999999L);

        List<Object[]> aaData = baseService.list(hql.toString(), paraMap, Integer.parseInt(start), Integer.parseInt(length));

        for (Object[] o : aaData) {
            o[8] = ((Date)o[8]).getTime();
            o[10] = (Integer)o[10] > 0 ? 2 : sup > 0 || (Integer)o[9] == userId ? 1 : 0;
            o[14] = o[14] == null ? 0 : 1;

            RemoteStatusType statusType = RemoteStatusType.valueOf((String)o[15]);
            o[15] = statusType == RemoteStatusType.AC ? 0 : statusType.finalized ? 1 : 2; // 0-green 1-red 2-purple

            int submissionId = (Integer) o[0];
            try {
                if (!statusType.finalized) {
                    long freezeThreshold = (statusType == RemoteStatusType.SUBMIT_FAILED_TEMP) ? Math.max(3600000L, System.currentTimeMillis() - (Long)o[8]) : 300000L;
                    long freezeLength = runningSubmissions.getFreezeLength(submissionId);
                    if ((freezeLength != Long.MAX_VALUE || statusType != RemoteStatusType.SUBMIT_FAILED_TEMP) && freezeLength > freezeThreshold) {
                        submission = (Submission) baseService.query(Submission.class, submissionId);
                        judgeService.rejudge(submission, false);
                    }
                }
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }

            o[16] = runningSubmissions.contains(submissionId) ? 1 : 0; // 1-working 0-notWorking
        }

        dataTablesPage.setData(aaData);
        dataTablesPage.setDraw(Integer.parseInt(draw));

        return SUCCESS;
    }

    public String toEditDescription(){
        Map session = ActionContext.getContext().getSession();
        List list = baseService.query("select d from Description d left join fetch d.problem where d.id = " + id);
        description = (Description) list.get(0);
        problem = description.getProblem();
        if (session.get("visitor") == null){
            return "login";
        }
        redir = ServletActionContext.getRequest().getHeader("Referer") + "&edit=1";
        return SUCCESS;
    }

    public String editDescription(){
        Map session = ActionContext.getContext().getSession();
        User user = (User) session.get("visitor");
        if (user == null){
            session.put("error", "Please login first!");
            return ERROR;
        }
        if (id == 0){
            return ERROR;
        }
        description.setDescription(Jsoup.clean(description.getDescription(), Whitelist.relaxed()));
        description.setInput(Jsoup.clean(description.getInput(), Whitelist.relaxed()));
        description.setOutput(Jsoup.clean(description.getOutput(), Whitelist.relaxed()));
        description.setSampleInput(Jsoup.clean(description.getSampleInput(), Whitelist.relaxed()));
        description.setSampleOutput(Jsoup.clean(description.getSampleOutput(), Whitelist.relaxed()));
        description.setHint(Jsoup.clean(description.getHint(), Whitelist.relaxed()));

        description.setUpdateTime(new Date());
        description.setAuthor(user.getUsername());
        description.setVote(0);
        description.setProblem(new Problem(id));
        baseService.execute("delete from Description d where d.author = '" + user.getUsername() + "' and d.problem.id = " + id);
        baseService.addOrModify(description);
        return SUCCESS;
    }

    public String deleteDescription(){
        User user = OnlineTool.getCurrentUser();
        if (user == null) {
            return ERROR;
        }
        Session session = baseService.getSession();
        Transaction tx = session.beginTransaction();
        try {
            description = (Description) session.get(Description.class, id);
            if (!description.getAuthor().equals("0") && (user.getSup() == 1 || user.getUsername().equals(description.getAuthor()))){
                Set<Cproblem> cproblems = description.getCproblems();
                if (cproblems.size() > 0) {
                    //需要把引用该描述的cproblem置为引用system crawler对应的描述
                    Set<Description> descriptions = description.getProblem().getDescriptions();
                    Description sysDescription = null;
                    for (Description description : descriptions) {
                        if (description.getAuthor().equals("0")) {
                            sysDescription = description;
                            break;
                        }
                    }
                    for (Cproblem cproblem : cproblems) {
                        cproblem.setDescription(sysDescription);
                    }
                }
                session.delete(description);
            }
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
        } finally {
            baseService.releaseSession(session);
        }
        return SUCCESS;
    }

    public String viewSource(){
        Map session = ActionContext.getContext().getSession();
        User user = (User) session.get("visitor");
        List list = baseService.query("select s from Submission s left join fetch s.contest left join fetch s.problem left join fetch s.user where s.id = " + id);
        if (list.isEmpty()){
            session.put("error", "No such submission!");
            return ERROR;
        }
        submission = (Submission) list.get(0);
        if (!(user != null && (user.getSup() != 0 || user.getId() == submission.getUser().getId()) || submission.getIsOpen() == 1 && (submission.getContest() == null || new Date().compareTo(submission.getContest().getEndTime()) > 0))){
            session.put("error", "No access to this code!");
            return ERROR;
        }
        problem = submission.getProblem();
        submission.setSource(Tools.toHTMLChar(submission.getSource()));
        languageList = languageManager.getLanguages(problem.getOriginOJ());
        //        languageList = (Map<Object, String>) ApplicationContainer.serveletContext.getAttribute(problem.getOriginOJ());
        submission.setLanguage(languageList.get(submission.getLanguage()));
        uid = submission.getUser().getId();
        un = submission.getUser().getUsername();

        //这里language用作为shjs提供语言识别所需要的class名
        language = Tools.findClass4SHJS(submission.getLanguage());

        return SUCCESS;
    }



    public String rejudge(){
        Map session = ActionContext.getContext().getSession();
        User user = (User) session.get("visitor");
        List<Submission> submissionList = baseService.query("select s from Submission s left join fetch s.problem where s.id = " + id);
        if (!submissionList.isEmpty()) {
            submission = submissionList.get(0);
        }
        if (submission == null){
            return ERROR;
        } else {
            judgeService.rejudge(submission, false);
            return SUCCESS;
        }
    }

    public String toggleOpen() {
        judgeService.toggleOpen(id);
        return SUCCESS;
    }

    public String fetchSubmissionInfo() {
        submission = (Submission) baseService.query(Submission.class, id);
        submissionInfo = submission.getAdditionalInfo();
        return SUCCESS;
    }

    public String visitOriginUrl() {
        List<String> _url = baseService.query("select p.url from Problem p where p.id = " + id);
        if (_url.isEmpty()) {
            return ERROR;
        } else {
            redir = _url.get(0);
            return SUCCESS;
        }
    }

    public int getUid() {
        return uid;
    }
    public void setUid(int uid) {
        this.uid = uid;
    }
    public Submission getSubmission() {
        return submission;
    }
    public void setSubmission(Submission submission) {
        this.submission = submission;
    }
    public int getIsOpen() {
        return isOpen;
    }
    public void setIsOpen(int isOpen) {
        this.isOpen = isOpen;
    }

    public List getDataList() {
        return dataList;
    }
    public void setDataList(List dataList) {
        this.dataList = dataList;
    }
    public Map<String, String> getLanguageList() {
        return languageList;
    }
    public void setLanguageList(Map<String, String> languageList) {
        this.languageList = languageList;
    }
    public String getRedir() {
        return redir;
    }
    public void setRedir(String redir) {
        this.redir = redir;
    }
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Problem getProblem() {
        return problem;
    }
    public void setProblem(Problem problem) {
        this.problem = problem;
    }
    public String getOJId() {
        return OJId;
    }
    public void setOJId(String id) {
        OJId = id;
    }
    public String getProbNum() {
        return probNum;
    }
    public void setProbNum(String probNum) {
        this.probNum = probNum;
    }
    public DataTablesPage getDataTablesPage() {
        return dataTablesPage;
    }
    public void setDataTablesPage(DataTablesPage dataTablesPage) {
        this.dataTablesPage = dataTablesPage;
    }
    public int getRes() {
        return res;
    }
    public void setRes(int res) {
        this.res = res;
    }
    public String getUn() {
        return un;
    }
    public void setUn(String un) {
        this.un = un;
    }
    public Description getDescription() {
        return description;
    }
    public void setDescription(Description description) {
        this.description = description;
    }
    public String get_64Format() {
        return _64Format;
    }
    public void set_64Format(String _64Format) {
        this._64Format = _64Format;
    }
    //    public String getProbNum1() {
    //        return probNum1;
    //    }
    //    public void setProbNum1(String probNum1) {
    //        this.probNum1 = probNum1;
    //    }
    //    public String getProbNum2() {
    //        return probNum2;
    //    }
    //    public void setProbNum2(String probNum2) {
    //        this.probNum2 = probNum2;
    //    }
    public Integer getIsSup() {
        return isSup;
    }
    public void setIsSup(Integer isSup) {
        this.isSup = isSup;
    }
    public String getSubmissionInfo() {
        return submissionInfo;
    }
    public void setSubmissionInfo(String submissionInfo) {
        this.submissionInfo = submissionInfo;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
