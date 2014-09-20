package judge.remote.loginer;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpBodyValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.common.RetentiveLoginer;

import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class SCULoginer extends RetentiveLoginer {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.SCU;
    }

    @Override
    protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) {
        if (client.get("/soj/index.action").getBody().contains("href=\"logout.action\"")) {
            return;
        }

        HttpEntity entity = SimpleNameValueEntityFactory.create( //
                "back", "2", //
                "id", account.getAccountId(), //
                "password", account.getPassword(), //
                "submit", "login");
        client.post("/soj/login.action", entity, new HttpBodyValidator("window.history.go(-"));
    }

}
