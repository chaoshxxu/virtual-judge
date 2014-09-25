package judge.remote.provider.nbut;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpBodyValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.loginer.RetentiveLoginer;
import judge.tool.Tools;

import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class NBUTLoginer extends RetentiveLoginer {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.NBUT;
    }

    @Override
    protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) {
        if (client.get("/").getBody().contains("title=\"登出\"")) {
            return;
        }

        String html = client.get("/User/login.xhtml?url=%2F").getBody();
        String ojVerify = Tools.regFind(html, "name=\"__OJVERIFY__\" value=\"(\\w+)\"");
        
        HttpEntity entity = SimpleNameValueEntityFactory.create( //
                "__OJVERIFY__", ojVerify, //
                "password", account.getPassword(), //
                "username", account.getAccountId());
        client.post("/User/chklogin.xhtml", entity, new HttpBodyValidator("1"));
    }

}
