package judge.remote.submitter;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleHttpResponse;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.submitter.common.CanonicalSubmitter;
import judge.remote.submitter.common.SubmissionInfo;
import judge.tool.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
public class UVASubmitter extends CanonicalSubmitter {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.UVA;
	}

	@Override
	protected boolean needLogin() {
		return true;
	}

	@Override
	protected Integer getMaxRunId(SubmissionInfo info, DedicatedHttpClient client, boolean submitted) {
		return submitted ? Integer.parseInt(info.remoteRunId) : -1;
	}

	@Override
	protected String submitCode(final SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
		HttpEntity entity = SimpleNameValueEntityFactory.create( //
				"problemid", "", //
				"category", "", //
				"localid", info.remoteProblemId, //
				"language", info.remotelanguage, //
				"code", info.sourceCode, //
				"codeupl", "");

		SimpleHttpResponse response = client.post(
				"/index.php?option=com_onlinejudge&Itemid=25&page=save_submission",
				entity,
				HttpStatusValidator.SC_MOVED_PERMANENTLY);

		String headerLocation = response.getRawResponse().getFirstHeader("Location").getValue();
		info.remoteRunId = Tools.regFind(headerLocation, "with\\+ID\\+(\\d+)");
		Validate.isTrue(!StringUtils.isBlank(info.remoteRunId));
		return null;
	}

}
