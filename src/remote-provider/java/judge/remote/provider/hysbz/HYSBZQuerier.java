package judge.remote.provider.hysbz;

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
public class HYSBZQuerier extends AuthenticatedQuerier {

    @Override
    public RemoteOjInfo getOjInfo() {
        return HYSBZInfo.INFO;
    }

    @Override
    protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
        String html = client.get("/JudgeOnline/status.php?&top=" + info.remoteRunId).getBody();
        Pattern pattern = Pattern.compile("<tr align=center class='evenrow'><td>" + info.remoteRunId + "<td>.+?<td>.+?<td>(.+?)<td>(.+?)<td>(.+?)<td>");
        Matcher matcher = pattern.matcher(html);
        Validate.isTrue(matcher.find());
        
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = matcher.group(1).replaceAll("<.*?>", "").replace('_', ' ').trim();
        status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);
        if (status.statusType == RemoteStatusType.AC) {
            status.executionMemory = Integer.parseInt(matcher.group(2).replaceAll("\\D", ""));
            status.executionTime = Integer.parseInt(matcher.group(3).replaceAll("\\D", ""));
        } else if (status.statusType == RemoteStatusType.CE) {
            html = client.get("/JudgeOnline/ceinfo.php?sid=" + info.remoteRunId).getBody();
            status.compilationErrorInfo = Tools.regFind(html, "(<pre>[\\s\\S]*?</pre>)");
        }
        return status;
    }

}
