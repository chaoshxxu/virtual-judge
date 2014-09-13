package judge.remote.querier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.querier.common.AuthenticatedQuerier;
import judge.remote.status.RemoteStatusType;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.status.SubstringNormalizer;
import judge.remote.submitter.common.SubmissionInfo;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class NBUTQuerier extends AuthenticatedQuerier {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.NBUT;
	}

	@Override
	protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
		SubmissionRemoteStatus status = new SubmissionRemoteStatus();
		String html = client.get("/Problem/viewcode.xhtml?submitid=" + info.remoteRunId, HttpStatusValidator.SC_OK).getBody();
		String regex = "当前状态: <span.*?>(.+?)</span>　运行时间: (\\d+?) ms　运行内存: (\\d+?) K";
		Matcher matcher = Pattern.compile(regex).matcher(html);
		Validate.isTrue(matcher.find());
		status.rawStatus = StringUtils.capitalize(matcher.group(1).replaceAll("<[^<>]*>", "").replace('_', ' ').trim().toLowerCase());
		status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);
		status.executionTime = Integer.parseInt(matcher.group(2));
		status.executionMemory = Integer.parseInt(matcher.group(3));
		if (status.statusType == RemoteStatusType.CE) {
			html = client.get("/Problem/viewce.xhtml?submitid=" + info.remoteRunId).getBody();
			status.compilationErrorInfo = Tools.regFind(html, "(<pre style=\"overflow-x: auto;\">[\\s\\S]*?</pre>)");
		}
		return status;
	}
	
}
