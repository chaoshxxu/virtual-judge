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
public class SPOJLoginer extends RetentiveLoginer {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.SPOJ;
    }

    @Override
    protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) {
        if (client.get("/").getBody().contains("<a href=\"/logout\">")) {
            return;
        }

        HttpEntity entity = SimpleNameValueEntityFactory.create( //
                "login_user", account.getAccountId(), //
                "password", account.getPassword(), //
                "autologin", "1", //
                "submit", "Log In", //
                "ISO-8859-1");
        client.post("/logout", entity, HttpStatusValidator.SC_OK);
    }

}
