package judge.remote.loginer;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.common.RetentiveLoginer;

import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class HUSTLoginer extends RetentiveLoginer {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.HUST;
    }

    @Override
    protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) {
        if (client.get("/").getBody().contains("/logout")) {
            return;
        }

        HttpEntity entity = SimpleNameValueEntityFactory.create( //
                "username", account.getAccountId(), //
                "pwd", account.getPassword(), //
                "code", "");
        client.post("/user/login", entity, HttpStatusValidator.SC_MOVED_TEMPORARILY);
    }

}
