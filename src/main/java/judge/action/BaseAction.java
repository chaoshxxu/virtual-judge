package judge.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import judge.remote.RemoteOjInfo;
import judge.remote.provider.aizu.AizuInfo;
import judge.remote.provider.codeforces.CodeForcesInfo;
import judge.remote.provider.csu.CSUInfo;
import judge.remote.provider.fzu.FZUInfo;
import judge.remote.provider.hdu.HDUInfo;
import judge.remote.provider.hust.HUSTInfo;
import judge.remote.provider.hysbz.HYSBZInfo;
import judge.remote.provider.lightoj.LightOJInfo;
import judge.remote.provider.nbut.NBUTInfo;
import judge.remote.provider.poj.POJInfo;
import judge.remote.provider.scu.SCUInfo;
import judge.remote.provider.sgu.SGUInfo;
import judge.remote.provider.spoj.SPOJInfo;
import judge.remote.provider.uestc.UESTCInfo;
import judge.remote.provider.uestc_old.UESTCOldInfo;
import judge.remote.provider.ural.URALInfo;
import judge.remote.provider.uva.UVAInfo;
import judge.remote.provider.uvalive.UVALiveInfo;
import judge.remote.provider.zoj.ZOJInfo;
import judge.remote.provider.ztrening.ZTreningInfo;
import judge.service.IBaseService;
import judge.service.JudgeService;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.struts2.interceptor.ParameterAware;

import com.opensymphony.xwork2.ActionSupport;

/**
 * 用于公共用途
 * @author Isun
 *
 */
public class BaseAction extends ActionSupport implements ParameterAware {

    private static final long serialVersionUID = 1L;
    
    protected Map<String, String[]> paraMap;

//    protected Integer start = 0;
//    protected Integer length = 25;
//    protected String sSearch;
//    protected Integer iSortingCols;
//    protected Integer iSortCol_0;
//    protected String sSortDir_0;
//    protected Integer draw;

    protected Object json;

    protected IBaseService baseService;
    protected JudgeService judgeService;

    static public List<RemoteOjInfo> OJList = new ArrayList<RemoteOjInfo>();
    static public List<String> OJListLiteral = new ArrayList<String>();
    static {
        OJList.add(POJInfo.INFO);
        OJList.add(ZOJInfo.INFO);
        OJList.add(UVALiveInfo.INFO);
        OJList.add(SGUInfo.INFO);
        OJList.add(URALInfo.INFO);
        OJList.add(HUSTInfo.INFO);
        OJList.add(SPOJInfo.INFO);
        OJList.add(HDUInfo.INFO);
        OJList.add(HYSBZInfo.INFO);
        OJList.add(UVAInfo.INFO);
        OJList.add(CodeForcesInfo.INFO);
        OJList.add(ZTreningInfo.INFO);
        OJList.add(AizuInfo.INFO);
        OJList.add(LightOJInfo.INFO);
        OJList.add(UESTCOldInfo.INFO);
        OJList.add(UESTCInfo.INFO);
        OJList.add(NBUTInfo.INFO);
        OJList.add(FZUInfo.INFO);
        OJList.add(CSUInfo.INFO);
        OJList.add(SCUInfo.INFO);
        Collections.sort(OJList, new Comparator<RemoteOjInfo>() {
            @Override
            public int compare(RemoteOjInfo oj1, RemoteOjInfo oj2) {
                return oj1.literal.compareTo(oj2.literal);
            }
        });
        
        for (RemoteOjInfo oj : OJList) {
            OJListLiteral.add(oj.toString());
        }
    }

//    static private List<String> OJListAll = new ArrayList<String>();
//    static {
//        OJListAll.add("All");
//        OJListAll.addAll(OJList);
//    }

    static public Map<String, String> lf = new HashMap<String, String>();
    static {
        lf.put("POJ", "%I64d & %I64u");
        lf.put("ZOJ", "%lld & %llu");
        lf.put("UVALive", "%lld & %llu");
        lf.put("SGU", "%I64d & %I64u");
        lf.put("URAL", "%I64d & %I64u");
        lf.put("HUST", "%lld & %llu");
        lf.put("SPOJ", "%lld & %llu");
        lf.put("HDU", "%I64d & %I64u");
        lf.put("HYSBZ", "%lld & %llu");
        lf.put("UVA", "%lld & %llu");
        lf.put("CodeForces", "%I64d & %I64u");
        lf.put("Z-Trening", "%lld & %llu");
        lf.put("Aizu", "%lld & %llu");
        lf.put("LightOJ", "%lld & %llu");
        lf.put("UESTC-old", "%lld & %llu");
        lf.put("UESTC", "%lld & %llu");
        lf.put("NBUT", "%I64d & %I64u");
        lf.put("FZU", "%I64d & %I64u");
        lf.put("CSU", "%lld & %llu");
        lf.put("SCU", "%lld & %llu");
    }
    
    @Override
    public void setParameters(Map<String, String[]> parameters) {
        this.paraMap= parameters;
    }
    
    protected String getParameter(String name) {
        String[] _value = paraMap.get(name);
        return ArrayUtils.isEmpty(_value) ? null : _value[0];
    }

//    public Integer getStart() {
//        return start;
//    }
//    public void setStart(Integer start) {
//        this.start = start;
//    }
//    public Integer getLength() {
//        return length;
//    }
//    public void setLength(Integer length) {
//        this.length = length;
//    }
//    public Integer getDraw() {
//        return draw;
//    }
//    public void setDraw(Integer draw) {
//        this.draw = draw;
//    }
//    public String getSSearch() {
//        return sSearch;
//    }
//    public void setSSearch(String search) {
//        sSearch = search;
//    }
//    public Integer getISortingCols() {
//        return iSortingCols;
//    }
//    public void setISortingCols(Integer sortingCols) {
//        iSortingCols = sortingCols;
//    }
//    public Integer getISortCol_0() {
//        return iSortCol_0;
//    }
//    public void setISortCol_0(Integer sortCol_0) {
//        iSortCol_0 = sortCol_0;
//    }
//    public String getSSortDir_0() {
//        return sSortDir_0;
//    }
//    public void setSSortDir_0(String sortDir_0) {
//        sSortDir_0 = sortDir_0;
//    }
    public List getOJList() {
        return OJList;
    }
//    public List getOJListAll() {
//        return OJListAll;
//    }
    public IBaseService getBaseService() {
        return baseService;
    }
    public void setBaseService(IBaseService baseService) {
        this.baseService = baseService;
    }
    public JudgeService getJudgeService() {
        return judgeService;
    }
    public void setJudgeService(JudgeService judgeService) {
        this.judgeService = judgeService;
    }
    public Object getJson() {
        return json;
    }
    public void setJson(Object json) {
        this.json = json;
    }

}
