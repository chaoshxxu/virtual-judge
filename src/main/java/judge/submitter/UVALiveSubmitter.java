package judge.submitter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import judge.tool.ApplicationContainer;
import judge.tool.Tools;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;


@SuppressWarnings("deprecation")
public class UVALiveSubmitter extends Submitter {

	static final String OJ_NAME = "UVALive";
	static private DefaultHttpClient clientList[];
	static public HttpParams params;
	static public ClientConnectionManager cm;
	static private boolean using[];
	static private String[] usernameList;
	static private String[] passwordList;

	private DefaultHttpClient httpClient;
	private HttpEntity entity;

	static {
		List<String> uList = new ArrayList<String>(), pList = new ArrayList<String>();
		try {
			FileReader fr = new FileReader(ApplicationContainer.sc.getRealPath("WEB-INF/classes/accounts.conf"));
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String info[] = br.readLine().split("\\s+");
				if (info.length >= 3 && info[0].equalsIgnoreCase(OJ_NAME)){
					uList.add(info[1]);
					pList.add(info[2]);
				}
			}
			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] xcs, String string) {}
				public void checkServerTrusted(X509Certificate[] xcs, String string) {}
				public X509Certificate[] getAcceptedIssuers() {return null;}
			};

			SSLContext sslcontext;
			sslcontext = SSLContext.getInstance("TLS");

			sslcontext.init(null, new TrustManager[]{tm}, null);

			SSLSocketFactory socketFactory = new SSLSocketFactory(sslcontext,SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
			// 不校验域名
			socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			Scheme sch = new Scheme("https", 443, socketFactory);

			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(sch);

			cm = new PoolingClientConnectionManager(schemeRegistry);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}

		usernameList = uList.toArray(new String[0]);
		passwordList = pList.toArray(new String[0]);
		using = new boolean[usernameList.length];
		clientList = new DefaultHttpClient[usernameList.length];
		HttpHost proxy = new HttpHost("127.0.0.1", 8087);
		for (int i = 0; i < clientList.length; i++){
			clientList[i] = new DefaultHttpClient(cm, params);
			clientList[i].getParams().setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.83 Safari/537.1");
			clientList[i].getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
			clientList[i].getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
			clientList[i].getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}

		Map<String, String> languageList = new TreeMap<String, String>();
		languageList.put("1", "ANSI C 4.5.3");
		languageList.put("2", "JAVA 1.6.0");
		languageList.put("3", "C++ 4.5.3");
		languageList.put("4", "PASCAL 2.4.0");
		sc.setAttribute("UVALive", languageList);
	}

	private void submit() throws Exception{
		HttpPost httpPost = new HttpPost("https://icpcarchive.ecs.baylor.edu/index.php?option=com_onlinejudge&Itemid=25&page=save_submission");

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("problemid", ""));
		nvps.add(new BasicNameValuePair("category", ""));
		nvps.add(new BasicNameValuePair("localid", submission.getOriginProb()));
		nvps.add(new BasicNameValuePair("language", submission.getLanguage()));
		nvps.add(new BasicNameValuePair("code", submission.getSource()));
		nvps.add(new BasicNameValuePair("codeupl", ""));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

		System.out.println("submit...");

		try {
			HttpResponse response = httpClient.execute(httpPost);
			entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_MOVED_PERMANENTLY){
				throw new RuntimeException();
			}
			String headerLocation = response.getFirstHeader("Location").getValue();
			String submissionId = Tools.regFind(headerLocation, "with\\+ID\\+(\\d+)");
			if (submissionId.isEmpty()) {
				throw new Exception();
			}
			submission.setRealRunId(submissionId);
			baseService.addOrModify(submission);
		} finally {
			EntityUtils.consume(entity);
		}
	}

	private void ensureLoggedIn(String username, String password) throws Exception{
		String indexContent = "";
		try {
			HttpGet httpGet = new HttpGet("https://icpcarchive.ecs.baylor.edu/index.php");
			HttpResponse response = httpClient.execute(httpGet);
			entity = response.getEntity();
			indexContent = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}

		if (indexContent.contains("mod_login_logoutform")) {
			return;
		}

		try {
			HttpPost httpost = new HttpPost("https://icpcarchive.ecs.baylor.edu/index.php?option=com_comprofiler&task=login");

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			String reg = "<input type=\"hidden\" name=\"([\\s\\S]*?)\" value=\"([\\s\\S]*?)\" />";
			Matcher matcher = Pattern.compile(reg).matcher(indexContent);
			int number = 0;
			while (matcher.find()){
				String name = matcher.group(1);
				String value = matcher.group(2);
				if (number > 0 && number < 9) {
					nvps.add(new BasicNameValuePair(name, value));
				}
				++number;
			}
			nvps.add(new BasicNameValuePair("remember", "yes"));
			nvps.add(new BasicNameValuePair("username", username));
			nvps.add(new BasicNameValuePair("passwd", password));

			httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

			HttpResponse response = httpClient.execute(httpost);
			entity = response.getEntity();
		} finally {
			EntityUtils.consume(entity);
		}
	}

	public void getResult(String username) throws Exception{
		String reg = "<td>" + submission.getRealRunId() + "</td>[\\s\\S]*?</td>[\\s\\S]*?</td>[\\s\\S]*?<td>([\\s\\S]*?)</td>[\\s\\S]*?</td>[\\s\\S]*?<td>([\\s\\S]*?)</td>", result;
		Pattern p = Pattern.compile(reg);

		HttpGet get = new HttpGet("https://icpcarchive.ecs.baylor.edu/index.php?option=com_onlinejudge&Itemid=9");
		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			String tLine = null;
			try {
				HttpResponse rsp = httpClient.execute(get);
				entity = rsp.getEntity();
				tLine = EntityUtils.toString(entity);
			} finally {
				EntityUtils.consume(entity);
			}

			Matcher m = p.matcher(tLine);
			if (m.find()) {
				result = m.group(1).replaceAll("<[\\s\\S]*?>", "").trim().replaceAll("judge", "judging").replaceAll("queue", "queueing").replaceAll("Received", "processing");
				if (result.isEmpty()) {
					result = "processing";
				}
				submission.setStatus(result);
				if (!result.contains("ing")){
					if (result.equals("Accepted")){
						submission.setTime(Integer.parseInt(m.group(2).replaceAll("\\.", "")));
					} else if (result.contains("Compilation error")) {
						getAdditionalInfo(submission.getRealRunId());
					}
					baseService.addOrModify(submission);
					return;
				}
				baseService.addOrModify(submission);
			}
			Thread.sleep(interval);
			interval += 500;
		}
		throw new Exception();
	}

	private void getAdditionalInfo(String runId) throws HttpException, IOException {
		HttpGet httpGet = new HttpGet("https://icpcarchive.ecs.baylor.edu/index.php?option=com_onlinejudge&Itemid=9&page=show_compilationerror&submission=" + runId);

		try {
			HttpResponse rsp = httpClient.execute(httpGet);
			entity = rsp.getEntity();
			String additionalInfo = EntityUtils.toString(entity);
			submission.setAdditionalInfo(Tools.regFind(additionalInfo, "Compilation error for submission " + runId + "</div>\\s*(<pre>[\\s\\S]*?</pre>)"));
		} finally {
			EntityUtils.consume(entity);
		}

	}

	private int getIdleClient() {
		int length = usernameList.length;
		int begIdx = (int) (Math.random() * length);

		while(true) {
			synchronized (using) {
				for (int i = begIdx, j; i < begIdx + length; i++) {
					j = i % length;
					if (!using[j]) {
						using[j] = true;
						httpClient = clientList[j];
						return j;
					}
				}
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void work() {
		idx = getIdleClient();
		int errorCode = 1;

		try {
			ensureLoggedIn(usernameList[idx], passwordList[idx]);
			submit();
			errorCode = 2;
			submission.setStatus("Running & Judging");
			baseService.addOrModify(submission);
			Thread.sleep(2000);
			getResult(usernameList[idx]);
		} catch (Exception e) {
			e.printStackTrace();
			submission.setStatus("Judging Error " + errorCode);
			baseService.addOrModify(submission);
		}
	}

	@Override
	public void waitForUnfreeze() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	//UVa Live貌似不限制每两次提交之间的提交间隔
		synchronized (using) {
			using[idx] = false;
		}
	}

}
