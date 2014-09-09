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
public class FZULoginer extends RetentiveLoginer {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.FZU;
	}

	@Override
	protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) {
		if (client.get("/index.php").getBody().contains(">Logout</a>")) {
			return;
		}

		HttpEntity entity = SimpleNameValueEntityFactory.create( //
				"uname", account.getAccountId(), //
				"upassword", account.getPassword());
		client.post("/login.php?act=1", entity, new HttpBodyValidator("location.replace(\"index.php\")"));
	}

}
