package judge.remote.querier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.remote.RemoteOj;
import judge.remote.querier.common.SyncQuerier;
import judge.remote.status.RemoteStatusType;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.status.SubstringNormalizer;
import judge.remote.submitter.common.SubmissionInfo;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class AizuQuerier extends SyncQuerier {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.Aizu;
	}

	@Override
	protected SubmissionRemoteStatus query(SubmissionInfo info) {
		DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOj().mainHost, null, getOj().defaultChaset);
		
		String html = client.get("/onlinejudge/status.jsp").getBody();
		String regex =
				"<td.*?#" + info.remoteRunId + "[\\s\\S]*?" +
				"<td[\\s\\S]*?" +
				"<td.*?" + info.remoteAccountId + "[\\s\\S]*?" +
				"<td[\\s\\S]*?" +
				"<td[\\s\\S]*?" +
				"<td.*?icon\\w+\">:([\\s\\S]*?)</span>[\\s\\S]*?" +
				"<td.*?>.*?</td>[\\s\\S]*?" +
				"<td.*?>.*?</td>[\\s\\S]*?" +
				"<td.*?>.*?</td>[\\s\\S]*?" +
				"<td.*?>(.*?)</td>[\\s\\S]*?" +
				"<td.*?>(.*?)</td>[\\s\\S]*?" +
				"<td.*?>.*?</td>[\\s\\S]*?" +
				"<td.*?>.*?</td>[\\s\\S]*?";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(html);
		Validate.isTrue(matcher.find());
		
		SubmissionRemoteStatus status = new SubmissionRemoteStatus();
		status.rawStatus = matcher.group(1).replaceAll("<[^<>]*>", "").trim();
		status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);
		
		if (status.statusType == RemoteStatusType.AC) {
			status.executionTime = calcTime(matcher.group(2));
			status.executionMemory = calcMemory(matcher.group(3));
		} else if (status.statusType == RemoteStatusType.CE) {
			html = client.get("/onlinejudge/compile_log.jsp?runID=" + info.remoteRunId).getBody();
			status.compilationErrorInfo = Tools.regFind(html, "</h3>([\\s\\S]+)");
		}
		return status;
	}
	
	private int calcTime(String str) {
		Matcher matcher = Pattern.compile("(\\d+):(\\d+)").matcher(str);
		if (matcher.find()) {
			Integer a = Integer.parseInt(matcher.group(1), 10);
			Integer b = Integer.parseInt(matcher.group(2), 10);
			return a * 1000 + b * 10;
		} else {
			return 0;
		}
	}
	
	private int calcMemory(String str) {
		String memory = Tools.regFind(str, "(\\d+)");
		return memory.isEmpty() ? 0 : Integer.parseInt(memory);
	}

}
