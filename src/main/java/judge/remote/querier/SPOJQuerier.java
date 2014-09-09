package judge.remote.querier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.querier.common.AuthenticatedQuerier;
import judge.remote.status.RemoteStatusNormalizer;
import judge.remote.status.RemoteStatusType;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.status.SubstringNormalizer;
import judge.remote.submitter.common.SubmissionInfo;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class SPOJQuerier extends AuthenticatedQuerier {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.SPOJ;
	}

	@Override
	protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
		String html = client.post( //
				"/status/ajax=1,ajaxdiff=1", //
				SimpleNameValueEntityFactory.create("ids", info.remoteRunId, getOj().defaultChaset) //
		).getBody();
		html = html.replaceAll("\\\\[nt]", "").replaceAll(">(run|edit)<", "><").replaceAll("<.*?>", "").replace("&nbsp;", "").trim();

		Pattern pattern = Pattern.compile("\"status_description\":\"(.+?)\", \"id\":" + info.remoteRunId + ", \"status\":.+?,\"time\":\"(.+?)\",\"mem\":\"(.+?)\",");
		Matcher matcher = pattern.matcher(html);
		Validate.isTrue(matcher.find());
		
		SubmissionRemoteStatus status = new SubmissionRemoteStatus();
		status.rawStatus = StringUtils.capitalize(matcher.group(1).trim());
		status.statusType = statusNormalizer.getStatusType(status.rawStatus);
		if (status.statusType == RemoteStatusType.AC) {
			int mul = matcher.group(3).contains("M") ? 1024 : 1;
			status.executionMemory = (int) (0.5 + mul * Double.parseDouble(matcher.group(3).replaceAll("[Mk]", "").trim()));
			status.executionTime = (int) (0.5 + 1000 * Double.parseDouble(matcher.group(2).trim()));
		} else if (status.statusType == RemoteStatusType.CE) {
			html = client.get("/error/" + info.remoteRunId).getBody();
			status.compilationErrorInfo = Tools.regFind(html, "<div align=\"left\">(<pre><small>[\\s\\S]*?</small></pre>)");
		}
		return status;
	}
	
	private static RemoteStatusNormalizer statusNormalizer = new SubstringNormalizer( //
			"Queuing", RemoteStatusType.QUEUEING, //
			"Compiling", RemoteStatusType.COMPILING, //
			"ing", RemoteStatusType.JUDGING, //
			"accepted", RemoteStatusType.AC, //
			"Presentation Error", RemoteStatusType.PE, //
			"Wrong Answer", RemoteStatusType.WA, //
			"time limit exceeded", RemoteStatusType.TLE, //
			"Memory Limit Exceed", RemoteStatusType.MLE, //
			"Output Limit Exceed", RemoteStatusType.OLE, //
			"Segmentation Fault", RemoteStatusType.RE, //
			"runtime error", RemoteStatusType.RE, //
			"compilation error", RemoteStatusType.CE //
	);

}
