package judge.remote.submitter.common;

import java.util.Date;

public class SubmissionReceipt {
    
    /**
     * If submitting failed, set to null
     */
    public String remoteRunId;
    
    public String remoteAccountId;
    
    /**
     * Only used when submit failed
     */
    public String errorStatus;
    
    public Date submitTime;


    public SubmissionReceipt(String remoteRunId, String remoteAccountId, String errorStatus) {
        if (remoteRunId == null) {
            this.errorStatus = errorStatus;
        } else {
            this.remoteRunId = remoteRunId;
            this.remoteAccountId = remoteAccountId;
            this.submitTime = new Date();
        }
        
    }
    
}
