package judge.remote.provider.uestc;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import judge.httpclient.DedicatedHttpClient;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.querier.AuthenticatedQuerier;
import judge.remote.status.RemoteStatusType;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.status.SubstringNormalizer;
import judge.remote.submitter.SubmissionInfo;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.springframework.stereotype.Component;

@Component
public class UESTCQuerier extends AuthenticatedQuerier {

    @Override
    public RemoteOjInfo getOjInfo() {
        return UESTCInfo.INFO;
    }

    @Override
    protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) throws UnsupportedEncodingException, JSONException {
        for (int page = 1; page <= 10; page++) {
            SubmissionRemoteStatus status = queryOnePage(info, remoteAccount, client, page);
            if (status != null) {
                return status;
            }
        }
        throw new RuntimeException(String.format("Can't find %s submission(%s)", getOjInfo(), info.remoteRunId));
    }
    
    @SuppressWarnings("unchecked")
    private SubmissionRemoteStatus queryOnePage(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client, int page) throws UnsupportedEncodingException, JSONException {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("currentPage", page);
        payload.put("orderAsc", "false");
        payload.put("orderFields", "statusId");
        payload.put("problemId", Integer.parseInt(info.remoteProblemId));
        payload.put("userName", info.remoteAccountId);
        
        HttpPost post = new HttpPost("/status/search");
        post.setEntity(new StringEntity(JSONUtil.serialize(payload)));
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");          
        
        String jsonString = client.execute(post).getBody();

        Map<String, Object> json = (Map<String, Object>) JSONUtil.deserialize(jsonString);
        List<Map<String, Object>> results = (List<Map<String, Object>>)json.get("list");
        for (Map<String, Object> result : results) {
            int runId = ((Long) result.get("statusId")).intValue();
            if (runId == Integer.parseInt(info.remoteRunId)) {
                SubmissionRemoteStatus status = new SubmissionRemoteStatus();

                status.rawStatus = (String) result.get("returnType");
                status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);
                if (status.statusType == RemoteStatusType.AC) {
                    status.executionMemory = ((Long) result.get("memoryCost")).intValue();
                    status.executionTime = ((Long) result.get("timeCost")).intValue();
                } else if (status.statusType == RemoteStatusType.CE) {
                    jsonString = client.post("/status/info/" + runId).getBody();
                    json = (Map<String, Object>) JSONUtil.deserialize(jsonString);
                    status.compilationErrorInfo = "<pre>" + json.get("compileInfo") + "</pre>";
                }
                
                return status;
            }
        }
        return null;
    }
    
}
