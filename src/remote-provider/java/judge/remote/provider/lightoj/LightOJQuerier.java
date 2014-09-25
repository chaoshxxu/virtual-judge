package judge.remote.provider.lightoj;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.querier.AuthenticatedQuerier;
import judge.remote.status.RemoteStatusNormalizer;
import judge.remote.status.RemoteStatusType;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.status.SubstringNormalizer;
import judge.remote.submitter.SubmissionInfo;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class LightOJQuerier extends AuthenticatedQuerier {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.LightOJ;
    }

    @Override
    protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        String html = client.get("/volume_showcode.php?sub_id=" + info.remoteRunId, HttpStatusValidator.SC_OK).getBody();
        String regex = "<td class=\"one\">([\\s\\S]*?)</td>";
        Matcher matcher = Pattern.compile(regex).matcher(html);
        for (int i = 0; i < 7; i++) {
            Validate.isTrue(matcher.find());
            if (i == 4) { // time
                String timeStr = matcher.group(1).trim();
                try {
                    status.executionTime = (int) (Double.parseDouble(timeStr) * 1000);
                } catch (Exception e) {}
            } else if (i == 5) { // memory
                String memoryStr = matcher.group(1).trim();
                try {
                    status.executionMemory = Integer.parseInt(memoryStr);
                } catch (Exception e) {}
            } if (i == 6) { // status
                status.rawStatus = matcher.group(1).replaceAll("<[^<>]*>", "").trim();
                status.statusType = statusNormalizer.getStatusType(status.rawStatus);
            }
        }
        
        if (status.statusType == RemoteStatusType.CE) {
            html = client.get("/volume_showcode.php?sub_id=" + info.remoteRunId).getBody();
            status.compilationErrorInfo = "<pre>" + Tools.regFind(html, "<textarea style=[^>]+>([\\s\\S]*?)</textarea>") + "</pre>";
        }
        
        return status;
    }
    
    private static RemoteStatusNormalizer statusNormalizer = new SubstringNormalizer( //
            "Not Judged Yet", RemoteStatusType.QUEUEING //
    );

    
}
