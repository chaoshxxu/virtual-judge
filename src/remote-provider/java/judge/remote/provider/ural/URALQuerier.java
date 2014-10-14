package judge.remote.provider.ural;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.querier.SyncQuerier;
import judge.remote.status.RemoteStatusType;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.status.SubstringNormalizer;
import judge.remote.submitter.SubmissionInfo;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class URALQuerier extends SyncQuerier {

    @Override
    public RemoteOjInfo getOjInfo() {
        return URALInfo.INFO;
    }

    @Override
    protected SubmissionRemoteStatus query(SubmissionInfo info) {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, null, getOjInfo().defaultChaset);
        String html = client.get("/status.aspx?space=1&from=" + info.remoteRunId).getBody();

        String regex = 
                "<TD.*?>" + info.remoteRunId + "</TD>" +
                ".+?" +
                "<TD class=\"verdict_\\w{2,}\">(.+?)</TD>" +
                "<TD.*?>(.+?)</TD>" +
                "<TD.*?>(.+?)</TD>" +
                "<TD.*?>(.+?)</TD>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        Validate.isTrue(matcher.find());

        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = matcher.group(1).replaceAll("<.*?>", "").trim();
        status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);

        try {
            status.failCase = Integer.parseInt(matcher.group(2).replaceAll("\\D+", ""));
        } catch (Exception e) {}
        
        try {
            status.executionTime = (int) (Double.parseDouble(matcher.group(3)) * 1000 + 0.5);
        } catch (Exception e) {}
        
        try {
            status.executionMemory = Integer.parseInt(matcher.group(4).replaceAll("\\D+", ""));
        } catch (Exception e) {}
        
        if (status.statusType == RemoteStatusType.CE) {
            String ceUrl = "/ce.aspx?id=" + info.remoteRunId;
            HttpEntity entity = SimpleNameValueEntityFactory.create(
                "Action", "login", //
                "Source", ceUrl, //
                "JudgeID", info.remoteAccountId //
            );
            client.post("/auth.aspx", entity, HttpStatusValidator.SC_MOVED_TEMPORARILY);
            html = client.get(ceUrl).getBody();
            status.compilationErrorInfo = "<pre>" + html + "</pre>";
        }
        return status;
    }
    
}
