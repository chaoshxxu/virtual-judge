package judge.remote.provider.acdream;

import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.SimpleHttpResponse;
import judge.httpclient.SimpleHttpResponseValidator;
import judge.httpclient.SimpleNameValueEntityFactory;
import judge.remote.RemoteOjInfo;
import judge.remote.account.RemoteAccount;
import judge.remote.submitter.CanonicalSubmitter;
import judge.remote.submitter.SubmissionInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ACdreamSubmitter extends CanonicalSubmitter {

    @Override
    public RemoteOjInfo getOjInfo() {
        return ACdreamInfo.INFO;
    }

    @Override
    protected boolean needLogin() {
        return true;
    }

    @Override
    protected Integer getMaxRunId(SubmissionInfo info, DedicatedHttpClient client, boolean submitted) {
        String html = client.get("/status?name=" + info.remoteAccountId + "&pid=" + info.remoteProblemId).getBody();
        Matcher matcher = Pattern.compile("class=\"\\w+\"><td>(\\d{5,})</td>").matcher(html);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    @Override
    protected String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client) {
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "lang", info.remotelanguage, //
                "pid", info.remoteProblemId, //
                "code", info.sourceCode
        );
        client.post("/submit", entity, new SimpleHttpResponseValidator() {
            @Override
            public void validate(SimpleHttpResponse response) throws Exception {
                Validate.isTrue(StringUtils.isEmpty(response.getBody()));
            }
        });
        return null;
    }

}
