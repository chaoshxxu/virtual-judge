package judge.remote.status;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import judge.bean.Submission;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class RunningSubmissions {
	
	/**
	 * submission.id -> submission
	 */
	private Map<Integer, Submission> records = new ConcurrentHashMap<Integer, Submission>();
	
	
	public boolean contains(int submissionId) {
		return records.containsKey(submissionId);
	}
	
	public Submission get(int submissionId) { 
		return records.get(submissionId);
	}
	
	public Submission add(Submission submission) {
		return records.put(submission.getId(), submission);
	}
	
	public Submission remove(int submissionId) {
		return records.remove(submissionId);
	}
	
	public String getLogKey(Submission submission) {
		if (submission == null) {
			return null;
		} else if (StringUtils.isBlank(submission.getRealRunId())) {
			return String.format(
					"#%d | %s - %s | %dB",
					submission.getId(),
					submission.getOriginOJ(),
					submission.getOriginProb(),
					StringUtils.length(submission.getSource()));
		} else {
			return String.format(
					"#%d(%s) | %s - %s | %dB | %s | %s",
					submission.getId(),
					submission.getRealRunId(),
					submission.getOriginOJ(),
					submission.getOriginProb(),
					StringUtils.length(submission.getSource()),
					submission.getStatus(),
					submission.getStatusCanonical());
		}
	}

}
