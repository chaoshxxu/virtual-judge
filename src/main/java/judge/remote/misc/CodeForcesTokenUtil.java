package judge.remote.misc;

import judge.httpclient.DedicatedHttpClient;
import judge.tool.Tools;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;

public class CodeForcesTokenUtil {
	
	public static class CodeForcesToken {
		public String _tta;
		public String csrf_token;
		
		public CodeForcesToken(String _tta, String csrf_token) {
			this._tta = _tta;
			this.csrf_token = csrf_token;
		}
	}

	/**
	 * Currently, get the following tokens: _tta, csrf_token
	 * @return
	 */
	public static CodeForcesToken getTokens(DedicatedHttpClient client) {
		String html = client.get("/").getBody();

		String csrfToken = Tools.regFind(html, "data-csrf='(\\w+)'");
		String _39ce7 = null;
		CookieStore cookieStore = (CookieStore) client.getContext().getAttribute(HttpClientContext.COOKIE_STORE);
		for (Cookie cookie : cookieStore.getCookies()) {
			if (cookie.getName().equals("39ce7")) {
				_39ce7 = cookie.getValue();
			}
		}
		Integer _tta = 0;
		for (int c = 0; c < _39ce7.length(); c++) {
			_tta = (_tta + (c + 1) * (c + 2) * _39ce7.charAt(c)) % 1009;
			if (c % 3 == 0)
				_tta++;
			if (c % 2 == 0)
				_tta *= 2;
			if (c > 0)
				_tta -= ((int) (_39ce7.charAt(c / 2) / 2)) * (_tta % 5);
			while (_tta < 0)
				_tta += 1009;
			while (_tta >= 1009)
				_tta -= 1009;
		}
		return new CodeForcesToken(_tta.toString(), csrfToken);
	}
}
