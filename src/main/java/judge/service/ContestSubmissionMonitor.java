package judge.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import judge.bean.Contest;
import judge.bean.Submission;
import judge.remote.status.RemoteStatusUpdateEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContestSubmissionMonitor implements RemoteStatusUpdateEvent.Listener {
	
	private Map<Integer, Long> updateTimes = new HashMap<Integer, Long>();

	@Autowired
	private BaseService baseService;

	@Autowired
	private JudgeService judgeService;

	@Autowired
	public ContestSubmissionMonitor(RemoteStatusUpdateEvent remoteStatusUpdateEvent) {
		remoteStatusUpdateEvent.addListener(this);
	}

	@Override
	public void onStatusUpdate(Submission submission) throws Exception {
		Contest contest = submission.getContest();
		if (contest == null || submission.getStatusUpdateTime() == null) {
			return;
		}
		long statusUpdateTime = submission.getStatusUpdateTime().getTime();
		Long originContestUpdateTime = updateTimes.get(contest.getId());
		if (originContestUpdateTime == null || originContestUpdateTime < statusUpdateTime) {
			updateTimes.put(contest.getId(), statusUpdateTime);
			contest = (Contest) baseService.query(Contest.class, contest.getId());
			judgeService.updateRankData(contest);
		}
	}
	
	/**
	 * -_-!
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public long getLastSubmissionUpdateTime(int contestId) {
		Long lastSubmissionUpdateTime = updateTimes.get(contestId);
		if (lastSubmissionUpdateTime == null) {
			List<Date> statusLastUpdateTimes = baseService.query("select max(s.statusUpdateTime) from Submission s where s.contest.id = " + contestId);
			try {
				updateTimes.put(contestId, statusLastUpdateTimes.get(0).getTime());
			} catch (Exception e) {
				updateTimes.put(contestId, 0L);
			}
		}
		return lastSubmissionUpdateTime == null ? 0 : lastSubmissionUpdateTime; 
	}

}
