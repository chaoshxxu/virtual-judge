package judge.remote.loginer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import judge.account.RemoteAccount;
import judge.httpclient.DedicatedHttpClient;
import judge.remote.loginer.common.Loginer;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

public class LightOJLoginer implements Loginer {

	@Override
	public String getOjName() {
		return "LightOJ";
	}

	@Override
	public void login(RemoteAccount account) {
		HttpHost host = new HttpHost("lightoj.com");
		DedicatedHttpClient client = new DedicatedHttpClient(host, account.getContext());
		
		String html = client.get("/index.php").getBody();
		if (!html.contains("<script>location.href=")) {
			return;
		}
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("mypassword", account.getPassword()));
		nvps.add(new BasicNameValuePair("myrem", "1"));
		nvps.add(new BasicNameValuePair("myuserid", account.getAccountId()));
		HttpEntity entity = new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8"));

		html = client.post("/login_check.php", entity).getBody();
		Validate.isTrue(html.contains("login_success.php"));
	}

}
