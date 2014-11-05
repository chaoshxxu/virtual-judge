package judge.remote.provider.acdream;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpBodyValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.RetentiveLoginer;
import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class ACdreamLoginer extends RetentiveLoginer {

    @Override
    public RemoteOjInfo getOjInfo() {
        return ACdreamInfo.INFO;
    }

    @Override
    protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) {
        if (client.get("/").getBody().contains(">Logout<")) {
            return;
        }

        HttpEntity entity = SimpleNameValueEntityFactory.create( //
                "remember", "true", //
                "password", account.getPassword(), //
                "username", account.getAccountId());
        client.post("/login", entity, new HttpBodyValidator("1", true));
    }

}
