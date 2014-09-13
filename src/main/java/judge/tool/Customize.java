package judge.tool;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import judge.bean.Contest;
import judge.bean.Cproblem;
import judge.bean.Description;
import judge.bean.ReplayStatus;
import judge.bean.Submission;
import judge.service.IBaseService;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class Customize {
	private final static Logger log = LoggerFactory.getLogger(Customize.class);

	static public ServletContext sc = ApplicationContainer.serveletContext;
	static public IBaseService baseService = (IBaseService) SpringBean.getBean("baseService");

	static public void recalculateContestHash() {
		Session session = baseService.getSession();
		Transaction tx = session.beginTransaction();
		try {
			List<Object[]> list = session.createQuery("select cp.contest.id, cp.problem.title from Cproblem cp order by cp.contest.id, cp.num").list();
			String contestSign = "";
			int problemCnt = 0;
			for (int i = 0; i < list.size(); i++) {
				Object[] info = list.get(i);
				Integer contestId = (Integer) info[0];
				String problemTitle = (String) info[1];

				if (i == 0 || !contestId.equals(list.get(i - 1)[0])) {
					problemCnt = 0;
					contestSign = "";
				}

				++problemCnt;
				contestSign += problemTitle.toLowerCase().replaceAll("&#\\d+;", "").replaceAll("\\W", "");

				if (i == list.size() - 1 || !contestId.equals(list.get(i + 1)[0])) {
					String hashCode = MD5.getMD5(contestSign + problemCnt);
					session.createQuery("update Contest contest set contest.hashCode = :hashCode where contest.id = :contestId").setParameter("hashCode", hashCode).setParameter("contestId", contestId).executeUpdate();
					log.info(i + "/" + list.size());
				}
			}
			session.flush();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			baseService.releaseSession(session);
		}
	}

	static public void deleteRedundantDescription() {
		List<Description> list = baseService.query("select d from Description d where d.author = '0' order by d.problem.id asc");
		Session session = baseService.getSession();
		Transaction tx = session.beginTransaction();
		try {
			for (int i = 0; i < list.size() - 1; i++) {
				Description thisDescription = list.get(i);
				Description nextDescription = list.get(i + 1);
				//log.info(thisDescription.getProblem().getId());
				if (thisDescription.getId() == nextDescription.getId()) {
					log.info(thisDescription.getProblem().getId() + "");
					session.delete(thisDescription);
				}
			}
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			baseService.releaseSession(session);
		}
	}

	static public void initCproblemDescription() {
		Description[] bestDescription = new Description[50000];
		List<Description> list = baseService.query("select d from Description d order by d.vote asc");
		for (Description description : list) {
			bestDescription[description.getProblem().getId()] = description;
		}
		List<Cproblem> list1 = baseService.query("select cp from Cproblem cp");
		for (Cproblem cproblem : list1) {
			cproblem.setDescription(bestDescription[cproblem.getProblem().getId()]);
		}
		baseService.addOrModify(list1);
	}

	static public void transReplay() throws Exception {
		List<ReplayStatus> list = baseService.query("select rs from ReplayStatus rs left join fetch rs.contests");
		for (ReplayStatus replayStatus : list) {
			String old = replayStatus.getData();
			replayStatus.setData(old.replaceFirst("\\[\\d+,\\{", "[{"));
		}
		baseService.addOrModify(list);
	}

	static public void fixSubmissionPrivacy() throws Exception{
		int interval = 3000;
		List<Object[]> list;
		List dataList;

		for (int i = 9500; i < 100000; i += interval){
			log.info(i + "");
			dataList = new ArrayList();
			list = baseService.query("select s, c from Submission s left join s.contest c where s.id between " + i + " and " + (i + interval - 1));
			for (Object[] o : list) {
				Submission s = (Submission) o[0];
				if (o[1] == null && s.getIsPrivate() != 0){
					s.setIsPrivate(0);
					dataList.add(s);
				}
			}
			baseService.addOrModify(dataList);
		}
	}

	static public void fixDescRemark() throws Exception{
		List<Description> list = baseService.query("select d from Description d");
		for (Description description : list) {
			if (description != null && "Initializatioin.".equals(description.getRemarks())){
				description.setRemarks("Initialization.");
			}
		}
		baseService.addOrModify(list);
	}

	static public void fillSubmissions() throws Exception{
		int interval = 3000;
		List<Object[]> list;
		List dataList;

		for (int i = 9500; i < 100000; i += interval){
			log.info(i + "");
			dataList = new ArrayList();
			list = baseService.query("select s, p.originOJ, p.originProb, c.password from Submission s left join s.problem p left join s.contest c where s.id between " + i + " and " + (i + interval - 1));
			for (Object[] o : list) {
				Submission s = (Submission) o[0];
				s.setOriginOJ((String) o[1]);
				s.setOriginProb((String) o[2]);
				s.setIsPrivate(o[3] == null ? 0 : 1);
				dataList.add(s);
			}
			baseService.addOrModify(dataList);
		}
	}

	static public void calcHashCode4Contests(){

		List<Contest> cList = baseService.query("select c from Contest c");
		for (Contest contest : cList) {
			List<Object[]> cpList = baseService.query("select cp.num, p.title from Cproblem cp, Problem p where cp.problem.id = p.id and cp.contest.id = " + contest.getId() +  " order by cp.num asc");
			StringBuffer hashCode = new StringBuffer();
			for (Object s[] : cpList) {
				hashCode.append(((String)s[1]).toLowerCase().replaceAll("\\W", ""));
			}
			hashCode.append(cpList.size());
			contest.setHashCode(MD5.getMD5(hashCode.toString().replaceAll("&#\\d+;", "")));
		}
		baseService.addOrModify(cList);
	}

	static public void xx(){
		//Do nothing
	}


	public void setBaseService(IBaseService baseService) {
		Customize.baseService = baseService;
	}

}
