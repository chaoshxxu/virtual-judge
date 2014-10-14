package judge.remote.provider.zoj;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.querier.AuthenticatedQuerier;
import judge.remote.status.RemoteStatusType;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.status.SubstringNormalizer;
import judge.remote.submitter.SubmissionInfo;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class ZOJQuerier extends AuthenticatedQuerier {

    @Override
    public RemoteOjInfo getOjInfo() {
        return ZOJInfo.INFO;
    }

    @Override
    protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
        String html = client.get("/onlinejudge/showRuns.do?contestId=1&lastId=" + (Integer.parseInt(info.remoteRunId) + 1)).getBody();
        Pattern pattern = Pattern.compile("<td class=\"runId\">" + info.remoteRunId + "[\\s\\S]*?judgeReply\\w{2,5}\">([\\s\\S]*?)</span>[\\s\\S]*?runTime\">([\\s\\S]*?)</td>[\\s\\S]*?runMemory\">([\\s\\S]*?)</td>");
        Matcher matcher = pattern.matcher(html);
        Validate.isTrue(matcher.find());
        
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = matcher.group(1).replaceAll("<[\\s\\S]*?>", "").trim();
        status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);
        if (status.statusType == RemoteStatusType.AC) {
            status.executionMemory = Integer.parseInt(matcher.group(3));
            status.executionTime = Integer.parseInt(matcher.group(2));
        } else if (status.statusType == RemoteStatusType.CE) {
            String wierdRunId = Tools.regFind(matcher.group(1), "submissionId=(\\d+)");
            html = client.get("/onlinejudge/showJudgeComment.do?submissionId=" + wierdRunId).getBody();
            status.compilationErrorInfo = "<pre>" + html + "</pre>";
        }
        return status;
    }

}
