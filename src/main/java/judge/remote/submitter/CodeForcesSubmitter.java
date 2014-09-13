package judge.remote.submitter;

import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.SimpleHttpResponse;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.misc.CodeForcesTokenUtil;
import judge.remote.misc.CodeForcesTokenUtil.CodeForcesToken;
import judge.remote.submitter.common.CanonicalSubmitter;
import judge.remote.submitter.common.SubmissionInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;


@Component
public class CodeForcesSubmitter extends CanonicalSubmitter {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.CodeForces;
	}

	@Override
	protected boolean needLogin() {
		return true;
	}

	@Override
	protected Integer getMaxRunId(SubmissionInfo info, DedicatedHttpClient client, boolean submitted) throws IOException {
		String html = client.get("/submissions/" + info.remoteAccountId).getBody();
		Matcher matcher = Pattern.compile("submissionId=\"(\\d+)\"(?:[\\s\\S](?!tr))*" + info.remoteProblemId).matcher(html);
		return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
	}

	@Override
	protected String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) throws IOException, InterruptedException {
		CodeForcesToken token = CodeForcesTokenUtil.getTokens(client);
		
		HttpEntity entity = SimpleNameValueEntityFactory.create( //
			"csrf_token", token.csrf_token, //
			"_tta", token._tta, //
			"action", "submitSolutionFormSubmitted", //
			"submittedProblemCode", info.remoteProblemId, //
			"programTypeId", info.remotelanguage, //
			"source", info.sourceCode + getRandomBlankString(), //
			"sourceFile", "", //
			"sourceCodeConfirmed", "true", //
			"doNotShowWarningAgain", "on" //
		);
		
		SimpleHttpResponse response = client.post(
			"/problemset/submit?csrf_token=" + token.csrf_token,
			entity
		);
		
		if (response.getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
			if (response.getBody().contains("error for__programTypeId")) {
				return "Language Rejected";
			}
			if (response.getBody().contains("error for__source")) {
				return "Source Code Error";
			}
			throw new RuntimeException();
		}
		Thread.sleep(3000);
		return null;
	}
	
	private String getRandomBlankString() {
		String string = "\n";
		int random = new Random().nextInt(Integer.MAX_VALUE);
		while (random > 0) {
			string += random % 2 == 0 ? ' ' : '\t';
			random /= 2;
		}
		return string;
	}

}
