package judge.remote.provider.uestc;

import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.SimpleHttpResponse;
import judge.httpclient.SimpleHttpResponseMapper;
import judge.httpclient.SimpleHttpResponseValidator;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.submitter.CanonicalSubmitter;
import judge.remote.submitter.SubmissionInfo;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.springframework.stereotype.Component;

@Component
public class UESTCSubmitter extends CanonicalSubmitter {

    @Override
    public RemoteOjInfo getOjInfo() {
        return UESTCInfo.INFO;
    }

    @Override
    protected boolean needLogin() {
        return true;
    }

    @Override
    protected Integer getMaxRunId(SubmissionInfo info, DedicatedHttpClient client, boolean submitted) throws UnsupportedEncodingException, JSONException {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("contestId", -1);
        payload.put("currentPage", 1);
        payload.put("orderAsc", "false");
        payload.put("orderFields", "statusId");
        payload.put("problemId", info.remoteProblemId);
        payload.put("result", 0);
        payload.put("userName", info.remoteAccountId);
        
        HttpPost post = new HttpPost("/status/search");
        post.setEntity(new StringEntity(JSONUtil.serialize(payload)));
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        
        return client.execute(post, new SimpleHttpResponseMapper<Integer>() {
            @SuppressWarnings("unchecked")
            @Override
            public Integer map(SimpleHttpResponse response) throws JSONException {
                Map<String, Object> json = (Map<String, Object>) JSONUtil.deserialize(response.getBody());
                List<Map<String, Object>> list = (List<Map<String, Object>>) json.get("list");
                if (list.isEmpty()) {
                    return -1;
                } else {
                    Map<String, Object> latest = list.get(0);
                    return ((Long) latest.get("statusId")).intValue();
                }
            }
        });
    }

    @Override
    protected String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) throws UnsupportedCharsetException, JSONException {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("codeContent", info.sourceCode);
        payload.put("contestId", null);
        payload.put("languageId", info.remotelanguage);
        payload.put("problemId", info.remoteProblemId);
        
        HttpPost post = new HttpPost("/status/submit");
        post.setEntity(new StringEntity(JSONUtil.serialize(payload), "UTF-8"));
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");          

        client.execute(post, new SimpleHttpResponseValidator() {
            @SuppressWarnings("unchecked")
            @Override
            public void validate(SimpleHttpResponse response) throws JSONException {
                Map<String, Object> json = (Map<String, Object>) JSONUtil.deserialize(response.getBody());
                Validate.isTrue(json.get("result").equals("success"));        
            }
        });
        return null;
    }

}
