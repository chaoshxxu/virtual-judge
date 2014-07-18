package judge.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import judge.service.IBaseService;
import judge.service.JudgeService;
import judge.spider.AizuSpider;
import judge.spider.CSUSpider;
import judge.spider.CodeForcesSpider;
import judge.spider.FZUSpider;
import judge.spider.HDUSpider;
import judge.spider.HUSTSpider;
import judge.spider.HYSBZSpider;
import judge.spider.LightOJSpider;
import judge.spider.NBUTSpider;
import judge.spider.POJSpider;
import judge.spider.SCUSpider;
import judge.spider.SGUSpider;
import judge.spider.SPOJSpider;
import judge.spider.Spider;
import judge.spider.UESTCOldSpider;
import judge.spider.UESTCSpider;
import judge.spider.URALSpider;
import judge.spider.UVALiveSpider;
import judge.spider.UVASpider;
import judge.spider.ZOJSpider;
import judge.spider.ZTreningSpider;
import judge.submitter.AizuSubmitter;
import judge.submitter.CSUSubmitter;
import judge.submitter.CodeForcesSubmitter;
import judge.submitter.FZUSubmitter;
import judge.submitter.HDUSubmitter;
import judge.submitter.HUSTSubmitter;
import judge.submitter.HYSBZSubmitter;
import judge.submitter.LightOJSubmitter;
import judge.submitter.NBUTSubmitter;
import judge.submitter.POJSubmitter;
import judge.submitter.SCUSubmitter;
import judge.submitter.SGUSubmitter;
import judge.submitter.SPOJSubmitter;
import judge.submitter.Submitter;
import judge.submitter.UESTCOldSubmitter;
import judge.submitter.UESTCSubmitter;
import judge.submitter.URALSubmitter;
import judge.submitter.UVALiveSubmitter;
import judge.submitter.UVASubmitter;
import judge.submitter.ZOJSubmitter;
import judge.submitter.ZTreningSubmitter;

import com.opensymphony.xwork2.ActionSupport;

/**
 * 用于公共用途
 * @author Isun
 *
 */
public class BaseAction extends ActionSupport{

	private static final long serialVersionUID = 1L;

	protected Integer iDisplayStart = 0;
	protected Integer iDisplayLength = 25;
	protected Integer iColumns;
	protected String sSearch;
	protected Boolean bEscapeRegex;
	protected Integer iSortingCols;
	protected Integer iSortCol_0;
	protected String sSortDir_0;
	protected String sEcho;

	protected Object json;

	protected IBaseService baseService;
	protected JudgeService judgeService;

	static public List<String> OJList = new ArrayList<String>();
	static {
		OJList.add("POJ");
		OJList.add("ZOJ");
		OJList.add("UVALive");
		OJList.add("SGU");
		OJList.add("URAL");
		OJList.add("HUST");
		OJList.add("SPOJ");
		OJList.add("HDU");
		OJList.add("HYSBZ");
		OJList.add("UVA");
		OJList.add("CodeForces");
		OJList.add("Z-Trening");
		OJList.add("Aizu");
		OJList.add("LightOJ");
		OJList.add("UESTC-old");
		OJList.add("UESTC");
		OJList.add("NBUT");
		OJList.add("FZU");
		OJList.add("CSU");
		OJList.add("SCU");
	}

	static private List<String> OJListAll = new ArrayList<String>();
	static {
		OJListAll.add("All");
		OJListAll.addAll(OJList);
	}

	static public Map<String, Spider> spiderMap = new HashMap<String, Spider>();
	static {
		spiderMap.put("POJ", new POJSpider());
		spiderMap.put("ZOJ", new ZOJSpider());
		spiderMap.put("UVALive", new UVALiveSpider());
		spiderMap.put("SGU", new SGUSpider());
		spiderMap.put("URAL", new URALSpider());
		spiderMap.put("HUST", new HUSTSpider());
		spiderMap.put("SPOJ", new SPOJSpider());
		spiderMap.put("HDU", new HDUSpider());
		spiderMap.put("HYSBZ", new HYSBZSpider());
		spiderMap.put("UVA", new UVASpider());
		spiderMap.put("CodeForces", new CodeForcesSpider());
		spiderMap.put("Z-Trening", new ZTreningSpider());
		spiderMap.put("Aizu", new AizuSpider());
		spiderMap.put("LightOJ", new LightOJSpider());
		spiderMap.put("UESTC-old", new UESTCOldSpider());
		spiderMap.put("UESTC", new UESTCSpider());
		spiderMap.put("NBUT", new NBUTSpider());
		spiderMap.put("FZU", new FZUSpider());
		spiderMap.put("CSU", new CSUSpider());
		spiderMap.put("SCU", new SCUSpider());
	}

	static public Map<String, Submitter> submitterMap = new HashMap<String, Submitter>();
	static {
		submitterMap.put("POJ", new POJSubmitter());
		submitterMap.put("ZOJ", new ZOJSubmitter());
		submitterMap.put("UVALive", new UVALiveSubmitter());
		submitterMap.put("SGU", new SGUSubmitter());
		submitterMap.put("URAL", new URALSubmitter());
		submitterMap.put("HUST", new HUSTSubmitter());
		submitterMap.put("SPOJ", new SPOJSubmitter());
		submitterMap.put("HDU", new HDUSubmitter());
		submitterMap.put("HYSBZ", new HYSBZSubmitter());
		submitterMap.put("UVA", new UVASubmitter());
		submitterMap.put("CodeForces", new CodeForcesSubmitter());
		submitterMap.put("Z-Trening", new ZTreningSubmitter());
		submitterMap.put("Aizu", new AizuSubmitter());
		submitterMap.put("LightOJ", new LightOJSubmitter());
		submitterMap.put("UESTC-old", new UESTCOldSubmitter());
		submitterMap.put("UESTC", new UESTCSubmitter());
		submitterMap.put("NBUT", new NBUTSubmitter());
		submitterMap.put("FZU", new FZUSubmitter());
		submitterMap.put("CSU", new CSUSubmitter());
		submitterMap.put("SCU", new SCUSubmitter());
	}

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


	public Integer getIDisplayStart() {
		return iDisplayStart;
	}
	public void setIDisplayStart(Integer displayStart) {
		iDisplayStart = displayStart;
	}
	public Integer getIDisplayLength() {
		return iDisplayLength;
	}
	public void setIDisplayLength(Integer displayLength) {
		iDisplayLength = displayLength;
	}
	public Integer getIColumns() {
		return iColumns;
	}
	public void setIColumns(Integer columns) {
		iColumns = columns;
	}
	public String getSSearch() {
		return sSearch;
	}
	public void setSSearch(String search) {
		sSearch = search;
	}
	public Boolean getBEscapeRegex() {
		return bEscapeRegex;
	}
	public void setBEscapeRegex(Boolean escapeRegex) {
		bEscapeRegex = escapeRegex;
	}
	public Integer getISortingCols() {
		return iSortingCols;
	}
	public void setISortingCols(Integer sortingCols) {
		iSortingCols = sortingCols;
	}
	public String getSEcho() {
		return sEcho;
	}
	public void setSEcho(String echo) {
		sEcho = echo;
	}
	public Integer getISortCol_0() {
		return iSortCol_0;
	}
	public void setISortCol_0(Integer sortCol_0) {
		iSortCol_0 = sortCol_0;
	}
	public String getSSortDir_0() {
		return sSortDir_0;
	}
	public void setSSortDir_0(String sortDir_0) {
		sSortDir_0 = sortDir_0;
	}
	public List getOJList() {
		return OJList;
	}
	public List getOJListAll() {
		return OJListAll;
	}
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
