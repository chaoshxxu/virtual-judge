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
public class ZOJLoginer extends RetentiveLoginer {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.ZOJ;
	}

	@Override
	protected void loginEnforce(RemoteAccount account, DedicatedHttpClient client) {
		if (client.get("/onlinejudge/").getBody().contains("/logout.do")) {
			return;
		}

		HttpEntity entity = SimpleNameValueEntityFactory.create( //
				"handle", account.getAccountId(), //
				"password", account.getPassword(), //
				"rememberMe", "on");
		client.post("/onlinejudge/login.do", entity, HttpStatusValidator.SC_MOVED_TEMPORARILY);
	}

}
