package judge.remote.querier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.remote.account.RemoteAccount;
import judge.remote.querier.common.AuthenticatedQuerier;
import judge.remote.status.RemoteStatusNormalizer;
import judge.remote.status.RemoteStatusType;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.status.SubstringNormalizer;
import judge.remote.submitter.common.SubmissionInfo;
import judge.tool.Tools;

import org.apache.commons.lang3.Validate;

public abstract class UVAStyleQuerier extends AuthenticatedQuerier {

	@Override
	protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
		int itemsLimit = 32;
		while (itemsLimit < 1000) {
			SubmissionRemoteStatus status = queryOnce(info, remoteAccount, client, itemsLimit);
			if (status != null) {
				return status;
			}
			itemsLimit *= 2;
		}
		throw new RuntimeException(String.format("Can't find %s submission(%s)", getOj(), info.remoteRunId));
	}
	
	private SubmissionRemoteStatus queryOnce(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client, int itemsLimit) {
		String html = client.get("/index.php?option=com_onlinejudge&Itemid=9&limitstart=0&limit=" + itemsLimit, HttpStatusValidator.SC_OK).getBody();
		Validate.isTrue(html.contains("My Submissions"));
		
		Pattern pattern = Pattern.compile(
						"<td>" + info.remoteRunId + "</td>\\s*" +
						"<td.+?</td>\\s*" + 
						"<td.+?</td>\\s*" + 
						"<td>(.*?)</td>\\s*" + 
						"<td.+?</td>\\s*" + 
						"<td>(.*?)</td>\\s*");
		Matcher matcher = pattern.matcher(html);
		if (!matcher.find()) {
			return null;
		}
		
		SubmissionRemoteStatus status = new SubmissionRemoteStatus();
		status.rawStatus = matcher.group(1).replaceAll("<.*?>", "").trim();
		
		status.statusType = statusNormalizer.getStatusType(status.rawStatus);
		if (status.statusType == RemoteStatusType.AC) {
			status.executionTime = (int) (Double.parseDouble(matcher.group(2)) * 1000 + 0.5);
		} else if (status.statusType == RemoteStatusType.CE) {
			html = client.get("/index.php?option=com_onlinejudge&Itemid=9&page=show_compilationerror&submission=" + info.remoteRunId).getBody();
			status.compilationErrorInfo = Tools.regFind(html, "Compilation error for submission \\d+</div>\\s*(<pre>[\\s\\S]*?</pre>)");
		}
		return status;
	}
	
	private static RemoteStatusNormalizer statusNormalizer = new SubstringNormalizer( //
			"queue", RemoteStatusType.QUEUEING, //
			"Sent to judge", RemoteStatusType.QUEUEING, //
			"Received", RemoteStatusType.QUEUEING, //
			"Compiling", RemoteStatusType.COMPILING, //
			"ing", RemoteStatusType.JUDGING, //
			"Accepted", RemoteStatusType.AC, //
			"Presentation Error", RemoteStatusType.PE, //
			"Wrong answer", RemoteStatusType.WA, //
			"Time limit exceeded", RemoteStatusType.TLE, //
			"Memory limit exceeded", RemoteStatusType.MLE, //
			"Output Limit Exceeded", RemoteStatusType.OLE, //
			"Runtime error", RemoteStatusType.RE, //
			"Compilation error", RemoteStatusType.CE //
	);
}
