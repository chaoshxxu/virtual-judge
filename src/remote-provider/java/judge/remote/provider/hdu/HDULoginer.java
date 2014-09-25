package judge.remote.provider.hdu;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.RetentiveLoginer;

import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class HDULoginer extends RetentiveLoginer {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.HDU;
    }

    @Override
    protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) {
        if (client.get("/").getBody().contains("href=\"/userloginex.php?action=logout\"")) {
            return;
        }

        HttpEntity entity = SimpleNameValueEntityFactory.create( //
                "username", account.getAccountId(), //
                "userpass", account.getPassword(), //
                "gb2312");
        client.post(
                "/userloginex.php?action=login&cid=0&notice=0",
                entity,
                HttpStatusValidator.SC_MOVED_TEMPORARILY);
    }

}
