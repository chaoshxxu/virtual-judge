package judge.tool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class PhysicalAddressTool {

	static public Map<String, String> addressMap = new ConcurrentHashMap<String, String>();

	static public String getPhysicalAddressTool(final String ip) {
		if (addressMap.containsKey(ip)) {
			return addressMap.get(ip);
		}
		addressMap.put(ip, "");
		new Thread(new Runnable() {
			public void run() {
				HttpClient httpClient = new HttpClient();
				GetMethod getMethod = new GetMethod("http://ip138.com/ips138.asp?ip=" + ip);
				try {
					httpClient.executeMethod(getMethod);
					String responceString = Tools.getHtml(getMethod, "GB2312");
					Matcher matcher = Pattern.compile("<li>本站主数据：(.+?)</li>").matcher(responceString);
					matcher.find();
					String physicalAddress = matcher.group(1);
					if (addressMap.size() >= 1000) {
						addressMap.clear();
					}
					addressMap.put(ip, physicalAddress);
				} catch (Exception e) {
					e.printStackTrace();
					addressMap.remove(ip);
				}
			}
		}).start();
		return "";
	}
}
