package judge.remote.provider.sgu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.remote.RemoteOjInfo;
import judge.remote.querier.SyncQuerier;
import judge.remote.status.RemoteStatusType;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.status.SubstringNormalizer;
import judge.remote.submitter.SubmissionInfo;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class SGUQuerier extends SyncQuerier {

    @Override
    public RemoteOjInfo getOjInfo() {
        return SGUInfo.INFO;
    }

    @Override
    protected SubmissionRemoteStatus query(SubmissionInfo info) {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, null, getOjInfo().defaultChaset);
        
        String html = client.get("/status.php?start=" + info.remoteRunId).getBody();
        Pattern pattern = Pattern.compile("<TD>" + info.remoteRunId + "</TD>[\\s\\S]*?<TD class=btab>([\\s\\S]*?)</TD>[\\s\\S]*?([\\d]*?) ms</TD><TD>([\\d]*?) kb</TD>");
        Matcher matcher = pattern.matcher(html);
        Validate.isTrue(matcher.find());
        
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = matcher.group(1).replaceAll("<[\\s\\S]*?>", "").trim();
        status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);
        
        if (status.statusType == RemoteStatusType.AC) {
            status.executionMemory = Integer.parseInt(matcher.group(3));
            status.executionTime = Integer.parseInt(matcher.group(2));
        } else if (status.statusType == RemoteStatusType.CE) {
            html = client.get("/cerror.php?id=" + info.remoteRunId).getBody();
            status.compilationErrorInfo = Tools.regFind(html, info.remoteRunId + "</TD><TD>(<pre>[\\s\\S]*?</pre>)");
        }
        
        matcher = Pattern.compile("on test (\\d+)").matcher(status.rawStatus);
        if (matcher.find()) {
            status.failCase  = Integer.parseInt(matcher.group(1));
        }
        
        return status;
    }
    
}
