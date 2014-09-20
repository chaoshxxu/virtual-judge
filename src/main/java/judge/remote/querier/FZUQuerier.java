package judge.remote.querier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.remote.RemoteOj;
import judge.remote.querier.common.SyncQuerier;
import judge.remote.status.RemoteStatusType;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.status.SubstringNormalizer;
import judge.remote.submitter.common.SubmissionInfo;
import judge.tool.Tools;

import org.springframework.stereotype.Component;

@Component
public class FZUQuerier extends SyncQuerier {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.FZU;
    }

    @Override
    protected SubmissionRemoteStatus query(SubmissionInfo info) {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOj().mainHost, null, getOj().defaultChaset);

        for (int page = 1; page <= 10; page++) {
            SubmissionRemoteStatus status = queryOnePage(page, info, client);
            if (status != null) {
                return status;
            }
        }
        throw new RuntimeException(String.format("Can't find %s submission(%s)", getOj(), info.remoteRunId));
    }
    
    private SubmissionRemoteStatus queryOnePage(int page, SubmissionInfo info, DedicatedHttpClient client) {
        String html = client.get("/log.php?pid=" + info.remoteProblemId + "&user="  + info.remoteAccountId + "&page=" + page).getBody();
        String regex =
                "<td>" + info.remoteRunId + "</td>\\s*" +
                "<td>.*?</td>\\s*" +
                "<td>([\\s\\S]*?)</td>\\s*" +
                "<td>.*?</td>\\s*" +
                "<td>.*?</td>\\s*" +
                "<td>(.*?)</td>\\s*" +
                "<td>(.*?)</td>\\s*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        if (!matcher.find()) {
            return null;
        }
        
        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = matcher.group(1).replaceAll("<[\\s\\S]*?>", "").trim();
        status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);
        if (status.statusType == RemoteStatusType.AC) {
            status.executionTime = Integer.parseInt(matcher.group(2).replaceAll("\\D+", ""));
            status.executionMemory = Integer.parseInt(matcher.group(3).replaceAll("\\D+", ""));
        } else if (status.statusType == RemoteStatusType.CE) {
            html = client.get("/ce.php?sid=" + info.remoteRunId).getBody();
            status.compilationErrorInfo = "<pre>" + Tools.regFind(html, "Information:</b><br /><font color=\"blue\" size=\"-1\">([\\s\\S]*?)</font>").replaceAll("\n", "<br />") + "</pre>";
        }
        return status;
    }
    
}
