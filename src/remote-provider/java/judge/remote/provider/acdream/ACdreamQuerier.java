package judge.remote.provider.acdream;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.querier.AuthenticatedQuerier;
import judge.remote.status.RemoteStatusType;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.status.SubstringNormalizer;
import judge.remote.submitter.SubmissionInfo;
import org.apache.struts2.json.JSONException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ACdreamQuerier extends AuthenticatedQuerier {

    private static final Map<String, String> statusMap = new HashMap<String, String>() {{
        put("0", "Pending...");
        put("1", "Running...");
        put("2", "Accepted");
        put("3", "Presentation Error");
        put("4", "Time Limit Exceeded");
        put("5", "Memory Limit Exceeded");
        put("6", "Wrong Answer");
        put("7", "Output Limit Exceeded");
        put("8", "Compilation Error");
        put("13", "Dangerous Code");
        put("14", "System Error");
    }};

    @Override
    public RemoteOjInfo getOjInfo() {
        return ACdreamInfo.INFO;
    }

    @Override
    protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) throws JSONException {
        String html = client.post("/status/info", SimpleNameValueEntityFactory.create(
                "rid", info.remoteRunId
        )).getBody();

        Map<String, String> json = new Gson().fromJson(html, new TypeToken<HashMap<String, String>>() {
        }.getType());
        String result = json.get("result");
        int time = Integer.parseInt(json.get("time"));
        int memory = Integer.parseInt(json.get("memory"));

        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        status.rawStatus = statusMap.containsKey(result) ? statusMap.get(result) : "Runtime Error";
        status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);
        if (status.statusType == RemoteStatusType.AC) {
            status.executionMemory = memory;
            status.executionTime = time;
        } else if (status.statusType == RemoteStatusType.CE) {
            status.compilationErrorInfo = "<pre>" + client.post("/status/CE", SimpleNameValueEntityFactory.create(
                    "rid", info.remoteRunId
            )).getBody() + "</pre>";
        }
        return status;
    }

}
