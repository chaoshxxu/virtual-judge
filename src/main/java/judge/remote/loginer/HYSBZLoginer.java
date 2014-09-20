package judge.remote.loginer;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.SimpleHttpResponse;
import judge.httpclient.SimpleHttpResponseValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.common.RetentiveLoginer;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class HYSBZLoginer extends RetentiveLoginer {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.HYSBZ;
    }

    @Override
    protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) {
        if (client.get("/JudgeOnline/").getBody().contains("<a href=logout.php>")) {
            return;
        }

        HttpEntity entity = SimpleNameValueEntityFactory.create( //
                "user_id", account.getAccountId(), //
                "password", account.getPassword());
        
        client.post("/JudgeOnline/login.php", entity, new SimpleHttpResponseValidator() {
            @Override
            public void validate(SimpleHttpResponse response) throws Exception {
                Validate.isTrue(response.getBody().contains("history.go(-2)"));
            }
        });
    }

}
