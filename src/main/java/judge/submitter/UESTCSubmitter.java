package judge.submitter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import judge.bean.Problem;
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleHttpResponse;
import judge.httpclient.SimpleHttpResponseMapper;
import judge.httpclient.SimpleHttpResponseValidator;
import judge.tool.ApplicationContainer;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

@SuppressWarnings("unchecked")
public class UESTCSubmitter extends Submitter {

	static final String OJ_NAME = "UESTC";
	static private boolean using[];
	static private String[] usernameList;
	static private String[] passwordList;
	static private HttpContext[] contexts;
	
	private DedicatedHttpClient client;
	private HttpHost host = new HttpHost("acm.uestc.edu.cn");

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
		usernameList = uList.toArray(new String[0]);
		passwordList = pList.toArray(new String[0]);
		using = new boolean[usernameList.length];
		contexts = new HttpContext[usernameList.length];
		for (int i = 0; i < contexts.length; i++){
			CookieStore cookieStore = new BasicCookieStore();
			contexts[i] = new BasicHttpContext();
			contexts[i].setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
		}

		Map<String, String> languageList = new TreeMap<String, String>();
		languageList.put("1", "C");
		languageList.put("2", "C++");
		languageList.put("3", "Java");
		sc.setAttribute(OJ_NAME, languageList);
	}

	private void getMaxRunId() throws ClientProtocolException, IOException, JSONException {
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("currentPage", null);
		payload.put("orderAsc", "false");
		payload.put("orderFields", "statusId");
		
		HttpPost post = new HttpPost("/status/search");
		post.setEntity(new StringEntity(JSONUtil.serialize(payload)));
		post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		
		maxRunId = client.execute(post, new SimpleHttpResponseMapper<Integer>() {
			@Override
			public Integer map(SimpleHttpResponse response) throws JSONException {
				Map<String, Object> json = (Map<String, Object>) JSONUtil.deserialize(response.getBody());
				Map<String, Object> latest = ((List<Map<String, Object>>)json.get("list")).get(0);
				return ((Long) latest.get("statusId")).intValue();
			}
		});
	}
	
	private void login(String username, String password) throws ClientProtocolException, IOException, JSONException {
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("password", password);
		payload.put("userName", username);

		HttpPost post = new HttpPost("/user/login");
		post.setEntity(new StringEntity(JSONUtil.serialize(payload)));
		post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");  		

		client.execute(post, HttpStatusValidator.SC_OK, new SimpleHttpResponseValidator() {
			@Override
			public void validate(SimpleHttpResponse response) throws JSONException {
				Map<String, Object> json = (Map<String, Object>) JSONUtil.deserialize(response.getBody());
				Validate.isTrue(json.get("result").equals("success"));
			}
		});
	}

	private boolean isLoggedIn() throws ClientProtocolException, IOException, JSONException {
		String jsonString = client.get("/data").getBody();
		Map<String, Object> json = (Map<String, Object>) JSONUtil.deserialize(jsonString);
		return json.containsKey("currentUser");
	}

	private void submit() throws ClientProtocolException, IOException, JSONException {
		Problem problem = (Problem) baseService.query(Problem.class, submission.getProblem().getId());
		
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("codeContent", submission.getSource());
		payload.put("contestId", null);
		payload.put("languageId", submission.getLanguage());
		payload.put("problemId", problem.getOriginProb());
		
		HttpPost post = new HttpPost("/status/submit");
		post.setEntity(new StringEntity(JSONUtil.serialize(payload), "UTF-8"));
		post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");  		

		client.execute(post, new SimpleHttpResponseValidator() {
			@Override
			public void validate(SimpleHttpResponse response) throws JSONException {
				Map<String, Object> json = (Map<String, Object>) JSONUtil.deserialize(response.getBody());
				Validate.isTrue(json.get("result").equals("success"));		
			}
		});
	}

	public void getResult(String username) throws Exception{
		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("currentPage", 1);
			payload.put("orderAsc", "false");
			payload.put("orderFields", "statusId");
			payload.put("userName", username);
			
			HttpPost post = new HttpPost("/status/search");
			post.setEntity(new StringEntity(JSONUtil.serialize(payload)));
			post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");  		
			
			String jsonString = client.execute(post).getBody();

			Map<String, Object> json = (Map<String, Object>) JSONUtil.deserialize(jsonString);
			Map<String, Object> latest = ((List<Map<String, Object>>)json.get("list")).get(0);
			Integer runId = ((Long) latest.get("statusId")).intValue();
			
			if (runId > maxRunId) {
				String result = (String) latest.get("returnType");
				submission.setStatus(result);
				submission.setRealRunId(runId.toString());
				if (!result.contains("ing")){
					if (result.equals("Accepted")){
						submission.setMemory(((Long) latest.get("memoryCost")).intValue());
						submission.setTime(((Long) latest.get("timeCost")).intValue());
					} else if (result.contains("Compilation Error")) {
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

	private void getAdditionalInfo(String runId) throws HttpException, IOException, JSONException {
		String jsonString = client.post("/status/info/" + runId).getBody();
		Map<String, Object> json = (Map<String, Object>) JSONUtil.deserialize(jsonString);
		submission.setAdditionalInfo((String) json.get("compileInfo"));
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
		client = new DedicatedHttpClient(host, contexts[idx]);
		
		int errorCode = 1;

		try {
			if (!isLoggedIn()) {
				login(usernameList[idx], passwordList[idx]);
			}
			getMaxRunId();
			submit();
			errorCode = 2;
			submission.setStatus("Running & Judging");
			baseService.addOrModify(submission);
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
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	//UESTC限制每两次提交之间至少隔???秒
		synchronized (using) {
			using[idx] = false;
		}
	}
}
