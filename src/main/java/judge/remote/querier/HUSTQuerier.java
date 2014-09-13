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

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class HUSTQuerier extends AuthenticatedQuerier {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.HUST;
	}

	@Override
	protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
		String html = client.get("/solution/source/" + info.remoteRunId, HttpStatusValidator.SC_OK).getBody();

		String regex = 
		        "<span class=\"badge\">(.*?)</span>\\s*Result[\\s\\S]*?" +
		        "<span class=\"badge\">(\\d+)ms</span>\\s*Time[\\s\\S]*?" +
		        "<span class=\"badge\">(\\d+)kb</span>\\s*Memory";
		Matcher matcher = Pattern.compile(regex).matcher(html);
		Validate.isTrue(matcher.find());
		
		SubmissionRemoteStatus status = new SubmissionRemoteStatus();
		status.rawStatus = matcher.group(1).trim();
		status.executionTime = Integer.parseInt(matcher.group(2));
		status.executionMemory = Integer.parseInt(matcher.group(3));
		status.statusType = SubstringNormalizer.DEFAULT.getStatusType(status.rawStatus);
		if (status.statusType == RemoteStatusType.CE) {
			status.compilationErrorInfo = (Tools.regFind(html, "(<pre class=\"col-sm-12 linenums\">[\\s\\S]*?</pre>)"));
		}
		return status;
	}
	
}
