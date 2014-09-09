package judge.remote.querier;

import judge.httpclient.DedicatedHttpClient;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.querier.common.AuthenticatedQuerier;
import judge.remote.status.SubmissionRemoteStatus;
import judge.remote.submitter.common.SubmissionInfo;

import org.springframework.stereotype.Component;

@Component
public class URALQuerier extends AuthenticatedQuerier {

	@Override
	public RemoteOj getOj() {
		return RemoteOj.URAL;
	}

	@Override
	protected SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
		// TODO
		return null;
	}
	
}
