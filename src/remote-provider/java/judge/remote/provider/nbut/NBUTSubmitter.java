package judge.remote.provider.nbut;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.HttpBodyValidator;
import judge.httpclient.HttpStatusValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOj;
import judge.remote.account.RemoteAccount;
import judge.remote.submitter.CanonicalSubmitter;
import judge.remote.submitter.SubmissionInfo;

@Component
public class NBUTSubmitter extends CanonicalSubmitter {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.NBUT;
    }

    @Override
    protected boolean needLogin() {
        return true;
    }

    @Override
    protected Integer getMaxRunId(SubmissionInfo info, DedicatedHttpClient client, boolean submitted) {
        String html = client.get("/Problem/status.xhtml?username=" + info.remoteAccountId + "&problemid=" + info.remoteProblemId).getBody();
        Matcher matcher = Pattern.compile("<td style=\"text-align: center;\">(\\d+)").matcher(html);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    @Override
    protected String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
        HttpEntity entity = SimpleNameValueEntityFactory.create( //
            "language", info.remotelanguage, //
            "id", info.remoteProblemId, //
            "code", info.sourceCode //
        );
        client.post("/Problem/submitok.xhtml", entity, HttpStatusValidator.SC_OK, new HttpBodyValidator("\"status\":1"));
        return null;
    }

}
