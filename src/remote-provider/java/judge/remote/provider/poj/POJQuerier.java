package judge.remote.provider.poj;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpBodyValidator;
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
public class POJQuerier extends AuthenticatedQuerier {

    @Override
    public RemoteOjInfo getOjInfo() {
        return POJInfo.INFO;
    }

    @Override
    protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
        String html = client.get(
                "/showsource?solution_id=" + info.remoteRunId,
                new HttpBodyValidator("<title>Error</title>", true)).getBody();
        
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = Tools.regFind(html, "<b>Result:</b>(.+?)</td>").replaceAll("<.*?>", "").trim();
        status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);
        if (status.statusType == RemoteStatusType.AC) {
            status.executionMemory = Integer.parseInt(Tools.regFind(html, "<b>Memory:</b> (\\d+)"));
            status.executionTime = Integer.parseInt(Tools.regFind(html, "<b>Time:</b> (\\d+)"));
        } else if (status.statusType == RemoteStatusType.CE) {
            html = client.get("/showcompileinfo?solution_id=" + info.remoteRunId).getBody();
            Validate.isTrue(html.contains("Compile Error"));
            status.compilationErrorInfo = Tools.regFind(html, "(<pre>[\\s\\S]*?</pre>)");
        }
        return status;
    }
    
}
