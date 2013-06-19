package judge.submitter;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import judge.bean.Problem;
import judge.tool.ApplicationContainer;
import judge.tool.Tools;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

public class SCUSubmitter extends Submitter {

	static final String OJ_NAME = "SCU";
	static private DefaultHttpClient clientList[];
	static private boolean using[];
	static private String[] usernameList;
	static private String[] passwordList;

	private DefaultHttpClient client;
	private HttpGet get;
	private HttpPost post;
	private HttpResponse response;
	private HttpEntity entity;
	private HttpHost host = new HttpHost("cstest.scu.edu.cn");
	private String html;

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
		clientList = new DefaultHttpClient[usernameList.length];
		HttpHost proxy = new HttpHost("127.0.0.1", 8087);
		for (int i = 0; i < clientList.length; i++){
			clientList[i] = new DefaultHttpClient();
			clientList[i].getParams().setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.83 Safari/537.1");
			clientList[i].getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
			clientList[i].getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
			clientList[i].getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}

		Map<String, String> languageList = new TreeMap<String, String>();
		languageList.put("C++ (G++-3)", "C++ (G++-3)");
		languageList.put("C (GCC-3)", "C (GCC-3)");
		languageList.put("JAVA", "JAVA");
		languageList.put("PASCAL (GPC)", "PASCAL (GPC)");
		sc.setAttribute("SCU", languageList);
	}

	private void getMaxRunId() throws ClientProtocolException, IOException {
		Pattern p = Pattern.compile("<td height=\"44\">(\\d+)</td>");

		try {
			get = new HttpGet("/soj/solutions.action");
			response = client.execute(host, get);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}

		Matcher m = p.matcher(html);
		if (m.find()) {
			maxRunId = Integer.parseInt(m.group(1));
			System.out.println("maxRunId : " + maxRunId);
		} else {
			throw new RuntimeException();
		}
	}

	private void submit(String username, String password) throws ClientProtocolException, IOException {
		Problem problem = (Problem) baseService.query(Problem.class, submission.getProblem().getId());
		
		try {
			post = new HttpPost("/soj/submit.action");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("problemId", problem.getOriginProb()));
			nvps.add(new BasicNameValuePair("validation", getCaptcha()));
			nvps.add(new BasicNameValuePair("userId", username));
			nvps.add(new BasicNameValuePair("password", password));
			nvps.add(new BasicNameValuePair("language", submission.getLanguage()));
			nvps.add(new BasicNameValuePair("source", submission.getSource()));
			
			post.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName("GBK")));
			
			response = client.execute(host, post);
			entity = response.getEntity();
			
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
				throw new RuntimeException();
			}
		} finally {
			EntityUtils.consume(entity);
		}
	}

	private String getCaptcha() throws ClientProtocolException, IOException {
		File captchaPic = new File("scu_captcha_" + idx + ".jpg");
		try {
			get = new HttpGet("/soj/validation_code");
			response = client.execute(host, get);
			entity = response.getEntity();
			
			FileOutputStream fos = new FileOutputStream(captchaPic);
		    entity.writeTo(fos);
		    fos.close();

		    BufferedImage img = ImageIO.read(captchaPic);
		    return SCUCaptchaRecognizer.recognize(img);
		} finally {
			EntityUtils.consume(entity);
			if (captchaPic.exists()) {
				captchaPic.delete();
			}
		}
	}

	public void getResult(String username) throws Exception{
		Pattern p = Pattern.compile(
			"<td height=\"44\">(\\d+)</td>\\s*" +
			"<td>.*?</td>\\s*" +
			"<td>.*?</td>\\s*" +
			"<td>.*?</td>\\s*" +
			"<td>[\\s\\S]*?</td>\\s*" +
			"<td>([\\s\\S]*?)</td>\\s*" +
			"<td>(\\d+)</td>\\s*" +
			"<td>(\\d+)</td>");

		long cur = new Date().getTime(), interval = 2000;
		while (new Date().getTime() - cur < 600000){
			try {
				get = new HttpGet("/soj/solutions.action?userId=" + username);
				response = client.execute(host, get);
				entity = response.getEntity();
				html = EntityUtils.toString(entity);
			} finally {
				EntityUtils.consume(entity);
			}

			Matcher m = p.matcher(html);
			if (m.find() && Integer.parseInt(m.group(1)) > maxRunId) {
				String result = m.group(2).replace("<BR>", " ").replaceAll("<.*?>", "").trim();
				submission.setStatus(result);
				submission.setRealRunId(m.group(1));
				if (!result.contains("ing")){
					if (result.equals("Accepted")){
						submission.setTime(Integer.parseInt(m.group(3)));
						submission.setMemory(Integer.parseInt(m.group(4)));
					} else if (result.contains("Compilation")) {
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
		try {
			get = new HttpGet("/soj/judge_message.action?id=" + runId);
			response = client.execute(host, get);
			entity = response.getEntity();
			html = EntityUtils.toString(entity);
		} finally {
			EntityUtils.consume(entity);
		}
		submission.setAdditionalInfo(Tools.regFind(html, "(<pre>[\\s\\S]*?</pre>)"));
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
						client = clientList[j];
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
			getMaxRunId();
			submit(usernameList[idx], passwordList[idx]);
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
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	//SCU限制每两次提交之间至少隔3秒
		synchronized (using) {
			using[idx] = false;
		}
	}
}


class SCUCaptchaRecognizer {
	
	public static String recognize(BufferedImage img) {
		StringBuilder ans = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			int minDiff = 999999;
			char digitAns = 0;
			for (int j = 0; j <= 9; j++) {
				int curDiff = 0;
				for (int y = 0; y < 9; y++) {
					for (int x = 0; x < 6; x++) {
						boolean pixel1 = digits[j][y].charAt(x) == '#';
						boolean pixel2 = img.getRGB(left[i] + x, 1 + y) < -1e7;
						if (pixel1 != pixel2) {
							++curDiff;
						}
					}
				}
				if (curDiff < minDiff) {
					minDiff = curDiff;
					digitAns = (char) ('0' + j);
				}
			}
			ans.append(digitAns);
		}
		return ans.toString();
	}
	
	private static int left[] = new int[]{4, 12, 20, 28};
	
	private static String[][] digits = new String[][]{
			{
				"  ##  ",
				" #  # ",
				"#    #",
				"#    #",
				"#    #",
				"#    #",
				"#    #",
				" #  # ",
				"  ##  "
			},
			{
			    "  ##  ",
			    " # #  ",
			    "   #  ",
			    "   #  ",
			    "   #  ",
			    "   #  ",
			    "   #  ",
			    "   #  ",
			    " #####"
			},
			{
			    "####  ",
			    "    # ",
			    "    # ",
			    "    # ",
			    "   #  ",
			    "  #   ",
			    " #    ",
			    "#     ",
			    "##### "
			},
			{
			    "####  ",
			    "    # ",
			    "    # ",
			    "   #  ",
			    " ##   ",
			    "   #  ",
			    "    # ",
			    "    # ",
			    "####  "
			},
			{
				"    # ",
				"   ## ",
				"  # # ",
				" #  # ",
				"#   # ",
				"######",
				"    # ",
				"    # ",
				"    # "
			},
			{
			    "##### ",
			    "#     ",
			    "#     ",
			    "###   ",
			    "   #  ",
			    "    # ",
			    "    # ",
			    "   #  ",
			    "###   "
			},
			{
				"  ####",
				" #    ",
				"#     ",
				"# ##  ",
				"##  # ",
				"#    #",
				"#    #",
				" #  # ",
				"  ##  "		
			},
			{
				"######",
				"     #",
				"    # ",
				"   #  ",
				"   #  ",
				"  #   ",
				"  #   ",
				" #    ",
				" #    "
			},
			{
				" #### ",
				"#    #",
				"#    #",
				" #  # ",
				" #### ",
				"#    #",
				"#    #",
				"#    #",
				" #### "   
			},
			{
			    "  ##  ",
			    " #  # ",
			    "#    #",
			    "#    #",
			    " #  ##",
			    "  ## #",
			    "     #",
			    "    # ",
			    "####  "
			}
		};
}
