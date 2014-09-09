package judge.remote.submitter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.submitter.common.SubmissionInfo;
import judge.remote.submitter.common.CanonicalSubmitter;

import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class URALSubmitter extends CanonicalSubmitter {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.URAL;
	}

	@Override
	protected boolean needLogin() {
		return false;
	}

	@Override
	protected Integer getMaxRunId(SubmissionInfo info, DedicatedHttpClient client, boolean submitted) {
		String html = client.get("/status.aspx?space=1&num=" + info.remoteProblemId + "&author=" + info.remoteAccountId.replaceAll("\\D", "")).getBody();
		Matcher matcher = Pattern.compile("getsubmit\\.aspx/(\\d+)").matcher(html);
		return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
	}

	@Override
	protected String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
		HttpEntity entity = SimpleNameValueEntityFactory.create( //
				"Action", "submit", //
				"Language", info.remotelanguage, //
				"ProblemNum", info.remoteProblemId, //
				"Source", info.sourceCode, //
				"JudgeID", remoteAccount.getAccountId(), //
				"SpaceID", "1" //
		);
		client.post("/submit.aspx", entity, HttpStatusValidator.SC_MOVED_TEMPORARILY);
		return null;
	}

}
