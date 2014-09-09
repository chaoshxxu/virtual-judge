package judge.remote.querier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.remote.RemoteOj;
import judge.remote.querier.common.SyncQuerier;
import judge.remote.status.RemoteStatusNormalizer;
import judge.remote.status.RemoteStatusType;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.status.SubstringNormalizer;
import judge.remote.submitter.common.SubmissionInfo;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class HDUQuerier extends SyncQuerier {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.HDU;
	}

	@Override
	protected SubmissionRemoteStatus query(SubmissionInfo info) {
		DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOj().mainHost, null, getOj().defaultChaset);
		
		String html = client.get("/status.php?first=" + info.remoteRunId).getBody();
		Pattern pattern = Pattern.compile(">" + info.remoteRunId + "</td><td>[\\s\\S]*?</td><td>([\\s\\S]*?)</td><td>[\\s\\S]*?</td><td>(\\d*?)MS</td><td>(\\d*?)K</td>");
		Matcher matcher = pattern.matcher(html);
		Validate.isTrue(matcher.find());
		
		SubmissionRemoteStatus status = new SubmissionRemoteStatus();
		status.rawStatus = matcher.group(1).replaceAll("<[\\s\\S]*?>", "").trim();
		status.statusType = statusNormalizer.getStatusType(status.rawStatus);
		
		if (status.statusType == RemoteStatusType.AC) {
			status.executionTime = Integer.parseInt(matcher.group(2));
			status.executionMemory = Integer.parseInt(matcher.group(3));
		} else if (status.statusType == RemoteStatusType.CE) {
			html = client.get("/viewerror.php?rid=" + info.remoteRunId).getBody();
			status.compilationErrorInfo = Tools.regFind(html, "(<pre>[\\s\\S]*?</pre>)");
		}
		return status;
	}
	
	private static RemoteStatusNormalizer statusNormalizer = new SubstringNormalizer( //
			"Queuing", RemoteStatusType.QUEUEING, //
			"Compiling", RemoteStatusType.COMPILING, //
			"Running", RemoteStatusType.JUDGING, //
			"ing", RemoteStatusType.JUDGING, //
			"Accepted", RemoteStatusType.AC, //
			"Presentation Error", RemoteStatusType.PE, //
			"Wrong Answer", RemoteStatusType.WA, //
			"Time Limit Exceeded", RemoteStatusType.TLE, //
			"Memory Limit Exceeded", RemoteStatusType.MLE, //
			"Output Limit Exceeded", RemoteStatusType.OLE, //
			"Runtime Error", RemoteStatusType.RE, //
			"Compilation Error", RemoteStatusType.CE //
	);

}
