package judge.remote.provider.csu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.remote.RemoteOj;
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
public class CSUQuerier extends AuthenticatedQuerier {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.CSU;
    }

    @Override
    protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
        String html = client.get("/OnlineJudge/status.php?&top=" + info.remoteRunId).getBody();
        Pattern pattern = Pattern.compile("<td>\\s*" + info.remoteRunId + "</td><td>.+?<td>.+?<td>(.+?)<td>(.+?)<td>(.+?)<td>");
        Matcher matcher = pattern.matcher(html);
        Validate.isTrue(matcher.find());
        
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = matcher.group(1).replaceAll("<.*?>", "").trim();
        status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);
        if (status.statusType == RemoteStatusType.AC) {
            status.executionMemory = Integer.parseInt(matcher.group(2).replaceAll("\\D", ""));
            status.executionTime = Integer.parseInt(matcher.group(3).replaceAll("\\D", ""));
        } else if (status.statusType == RemoteStatusType.CE) {
            html = client.get("/OnlineJudge/ceinfo.php?sid=" + info.remoteRunId).getBody();
            status.compilationErrorInfo = Tools.regFind(html, "(<pre[\\s\\S]*?</pre>)");
        }
        
        return status;
    }

}
