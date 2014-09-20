package judge.remote.status;

/**
 * submission remote status type
 * 
 * @author Isun
 *
 */
public enum RemoteStatusType {
    
    PENDING(false), // to submit to remote OJ

    SUBMIT_FAILED_TEMP(false), // failed submitting to remote OJ, due to unknown reason
    SUBMIT_FAILED_PERM(true), // failed submitting to remote OJ, due to known reason
    
    SUBMITTED(false), // submitted to remote OJ
    
    QUEUEING(false), // queuing in remote OJ 
    COMPILING(false), // compiling in remote OJ
    JUDGING(false), // judging in remote OJ
    
    AC(true),
    PE(true),
    WA(true),
    TLE(true),
    MLE(true),
    OLE(true),
    RE(true),
    CE(true),
    FAILED_OTHER(true),
    
    
    ;
    
    
    public boolean finalized;
    RemoteStatusType(boolean finalized) {
        this.finalized = finalized;
    }

}
