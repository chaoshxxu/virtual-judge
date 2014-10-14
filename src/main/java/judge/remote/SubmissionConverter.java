package judge.remote;

import judge.bean.Submission;
import judge.remote.submitter.SubmissionInfo;

public class SubmissionConverter {
    
    public static SubmissionInfo toInfo(Submission submission) {
        SubmissionInfo info = new SubmissionInfo();
        info.remoteAccountId = submission.getRemoteAccountId();
        info.remotelanguage = submission.getLanguage();
        info.remoteOj = RemoteOj.valueOf(submission.getOriginOJ());
        info.remoteProblemId = submission.getOriginProb();
        info.remoteRunId = submission.getRealRunId();
        info.remoteSubmitTime = submission.getRemoteSubmitTime();
        info.sourceCode = submission.getSource();
        return info;
    }

}
