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
public class CSULoginer extends RetentiveLoginer {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.CSU;
    }

    @Override
    protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) {
        if (client.get("/OnlineJudge/include/profile.php").getBody().contains("Logout")) {
            return;
        }

        HttpEntity entity = SimpleNameValueEntityFactory.create( //
                "user_id", account.getAccountId(), //
                "password", account.getPassword());
        client.post("/OnlineJudge/login.php", entity, new HttpBodyValidator("history.go(-2)"));
    }

}
