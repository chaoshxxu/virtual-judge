package judge.remote.provider.scu;

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
public class SCUQuerier extends AuthenticatedQuerier {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.SCU;
    }

    @Override
    protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
        String html = client.get("/soj/solutions.action?from=" + info.remoteRunId).getBody();
        
        Pattern pattern = Pattern.compile(
                "<td height=\"44\">"+ info.remoteRunId + "</td>\\s*" +
                "<td>.*?</td>\\s*" +
                "<td>.*?</td>\\s*" +
                "<td>.*?</td>\\s*" +
                "<td>[\\s\\S]*?</td>\\s*" +
                "<td>([\\s\\S]*?)</td>\\s*" +
                "<td>(\\d+)</td>\\s*" +
                "<td>(\\d+)</td>");
        Matcher matcher = pattern.matcher(html);
        Validate.isTrue(matcher.find());
        
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = matcher.group(1).replace("<BR>", " ").replaceAll("<.*?>", "").trim();
        status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);
        if (status.statusType == RemoteStatusType.AC) {
            status.executionTime = Integer.parseInt(matcher.group(2));
            status.executionMemory = Integer.parseInt(matcher.group(3));
        } else if (status.statusType == RemoteStatusType.CE) {
            html = client.get("/soj/judge_message.action?id=" + info.remoteRunId).getBody();
            status.compilationErrorInfo = Tools.regFind(html, "(<pre>[\\s\\S]*?</pre>)");
        }
        
        return status;
    }

}
