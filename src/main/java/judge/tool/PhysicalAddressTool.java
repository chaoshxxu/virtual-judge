package judge.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import judge.executor.ExecutorTaskType;
import judge.executor.Task;
import judge.httpclient.DedicatedHttpClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;

public class PhysicalAddressTool {

	static private Map<String, String> addressMap = new ConcurrentHashMap<String, String>();

	static public String getPhysicalAddress(final String ips) {
		List<String> locations = new ArrayList<String>();
		for (String ip : ips.split("[^\\w\\.:]+")) {
			locations.add(getPhysicalAddressOne(ip));
		}
		return StringUtils.join(locations, ", ");
	}
	
	static private String getPhysicalAddressOne(final String ip) {
		if (addressMap.containsKey(ip)) {
			return addressMap.get(ip);
		}
		addressMap.put(ip, "N/A");
		new Task<String>(ExecutorTaskType.GENERAL) {
			@Override
			public String call() throws Exception {
				HttpHost host = new HttpHost("www.ip138.com");
				DedicatedHttpClient client = new DedicatedHttpClient(host, "gb2312");
				String html = client.get("/ips138.asp?ip=" + ip).getBody();
				String physicalAddress = Tools.regFind(html, "<li>本站主数据：(.+?)</li>");
				if (!StringUtils.isBlank(physicalAddress)) {
					addressMap.put(ip, physicalAddress);
				}
				if (addressMap.size() > 5000) {
					addressMap.clear();
				}
				return null;
			}
		}.submit();
		return addressMap.get(ip);
	}
	
	static public int getIpMapSize() {
		return addressMap.size();
	}
	
}
