package judge.remote.loginer;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpBodyValidator;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.common.RetentiveLoginer;

import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class AizuLoginer extends RetentiveLoginer {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.Aizu;
    }

    @Override
    protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) {
        if (client.get("/onlinejudge/index.jsp").getBody().contains("href=\"logout.jsp\"")) {
            return;
        }

        HttpEntity entity = SimpleNameValueEntityFactory.create( //
                "loginUserID", account.getAccountId(), //
                "loginPassword", account.getPassword(), //
                "submit", "Sign in");
        client.post(
                "/onlinejudge/index.jsp",
                entity,
                HttpStatusValidator.SC_OK,
                new HttpBodyValidator("href=\"logout.jsp\""));
    }

}
