package judge.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import judge.remote.RemoteOj;
import judge.service.IBaseService;
import judge.service.JudgeService;

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
		OJList.add(RemoteOj.POJ.toString());
		OJList.add(RemoteOj.ZOJ.toString());
		OJList.add(RemoteOj.UVALive.toString());
		OJList.add(RemoteOj.SGU.toString());
		OJList.add(RemoteOj.URAL.toString());
		OJList.add(RemoteOj.HUST.toString());
		OJList.add(RemoteOj.SPOJ.toString());
		OJList.add(RemoteOj.HDU.toString());
		OJList.add(RemoteOj.HYSBZ.toString());
		OJList.add(RemoteOj.UVA.toString());
		OJList.add(RemoteOj.CodeForces.toString());
		OJList.add(RemoteOj.ZTrening.toString());
		OJList.add(RemoteOj.Aizu.toString());
		OJList.add(RemoteOj.LightOJ.toString());
		OJList.add(RemoteOj.UESTCOld.toString());
		OJList.add(RemoteOj.UESTC.toString());
		OJList.add(RemoteOj.NBUT.toString());
		OJList.add(RemoteOj.FZU.toString());
		OJList.add(RemoteOj.CSU.toString());
		OJList.add(RemoteOj.SCU.toString());
	}

	static private List<String> OJListAll = new ArrayList<String>();
	static {
		OJListAll.add("All");
		OJListAll.addAll(OJList);
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
