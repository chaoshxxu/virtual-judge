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

    /**
     * submission.id -> max(lastRemoveTime, lastAddTime) (in milliseconds)
     */
    private Map<Integer, Long> lastActiveTimes = new ConcurrentHashMap<Integer, Long>();
    
    ///////////////////////////////////////////////////////////////////////////////
    
    public boolean contains(int submissionId) {
        return records.containsKey(submissionId);
    }
    
    /**
     * Time since last add or remove
     * @param submissionId
     * @return milliseconds
     */
    public long getFreezeLength(int submissionId) {
        Long last = lastActiveTimes.get(submissionId);
        return last == null ? Long.MAX_VALUE : System.currentTimeMillis() - last;
    }
    
    public Submission get(int submissionId) { 
        return records.get(submissionId);
    }
    
    public Submission add(Submission submission) {
        if (lastActiveTimes.size() > 5000) {
            lastActiveTimes.clear();
        }
        lastActiveTimes.put(submission.getId(), System.currentTimeMillis());
        return records.put(submission.getId(), submission);
    }
    
    public Submission remove(int submissionId) {
        if (lastActiveTimes.size() > 5000) {
            lastActiveTimes.clear();
        }
        lastActiveTimes.put(submissionId, System.currentTimeMillis());
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
