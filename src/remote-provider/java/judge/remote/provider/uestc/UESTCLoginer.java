package judge.remote.provider.uestc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleHttpResponse;
import judge.httpclient.SimpleHttpResponseValidator;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.RetentiveLoginer;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import org.springframework.stereotype.Component;

@Component
public class UESTCLoginer extends RetentiveLoginer {

    @Override
    public RemoteOjInfo getOjInfo() {
        return UESTCInfo.INFO;
    }

    @Override
    protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) throws ClientProtocolException, IOException, JSONException {
        if (isLoggedIn(client)) {
            return;
        }

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("password", account.getPassword());
        payload.put("userName", account.getAccountId());

        HttpPost post = new HttpPost("/user/login");
        post.setEntity(new StringEntity(JSONUtil.serialize(payload)));
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");          

        client.execute(post, HttpStatusValidator.SC_OK, new SimpleHttpResponseValidator() {
            @SuppressWarnings("unchecked")
            @Override
            public void validate(SimpleHttpResponse response) throws JSONException {
                Map<String, Object> json = (Map<String, Object>) JSONUtil.deserialize(response.getBody());
                Validate.isTrue(json.get("result").equals("success"));
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    private boolean isLoggedIn(DedicatedHttpClient client) throws ClientProtocolException, IOException, JSONException {
        String jsonString = client.get("/data").getBody();
        Map<String, Object> json = (Map<String, Object>) JSONUtil.deserialize(jsonString);
        return json.containsKey("currentUser");
    }

}
