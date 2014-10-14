package judge.remote.submitter;

import java.util.HashMap;

import judge.executor.ExecutorTaskType;
import judge.httpclient.DedicatedHttpClient;
import judge.httpclient.DedicatedHttpClientFactory;
import judge.remote.account.RemoteAccount;
import judge.remote.account.RemoteAccountTask;
import judge.remote.language.LanguageFinder;
import judge.remote.language.LanguageFindersHolder;
import judge.remote.loginer.LoginersHolder;
import judge.tool.Handler;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * On many OJs, after submitting, we can't get the remote run ID from the direct
 * response of submitting request. Instead, to confirm submitting succeeds, we
 * need remember the max run ID before submitting and check if the new run ID
 * after submitting is larger than the previous one.
 * 
 * @author Isun
 * 
 */
public abstract class CanonicalSubmitter implements Submitter {
    private final static Logger log = LoggerFactory.getLogger(CanonicalSubmitter.class);

    @Autowired
    private DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public void submitCode(SubmissionInfo info, Handler<SubmissionReceipt> handler) throws Exception {
        new SubmitTask(info, handler).submit();
    }

    class SubmitTask extends RemoteAccountTask<SubmissionReceipt> {
        private SubmissionInfo info;

        public SubmitTask(SubmissionInfo info, Handler<SubmissionReceipt> handler) {
            super( //
                    ExecutorTaskType.SUBMIT_CODE, //
                    getOjInfo().remoteOj, //
                    info.remoteAccountId, //
                    "SUBMIT_CODE_" + info.remoteProblemId, //
                    handler //
                    );
            this.info = info;
        }

        @Override
        protected SubmissionReceipt call(RemoteAccount remoteAccount) throws Exception {
            Validate.isTrue(info.remoteAccountId == null || info.remoteAccountId.equals(remoteAccount.getAccountId()));
            info.remoteAccountId = remoteAccount.getAccountId();

            LanguageFinder languageFinder = LanguageFindersHolder.getLanguageFinder(getOjInfo().remoteOj);
            HashMap<String, String> languageAdapter = languageFinder.getLanguagesAdapter();
            if (languageAdapter != null && languageAdapter.containsKey(info.remotelanguage)) {
                info.remotelanguage = languageAdapter.get(info.remotelanguage);
            }

            if (needLogin()) {
                LoginersHolder.getLoginer(getOjInfo().remoteOj).login(remoteAccount);
            }
            DedicatedHttpClient client = dedicatedHttpClientFactory.build(getHost(), remoteAccount.getContext(),
                    getCharset());

            Integer runIdBefore = getMaxRunId(info, client, false);

            String errorStatus = submitCode(info, remoteAccount, client);
            if (errorStatus != null) {
                return new SubmissionReceipt(null, null, errorStatus);
            }

            Integer runIdAfter = null;
            long beginTime = System.currentTimeMillis();
            while (true) {
                runIdAfter = getMaxRunId(info, client, true);
                if (isRunIdValid(runIdBefore, runIdAfter)
                        || System.currentTimeMillis() - beginTime > getSubmitReceiptDelay()) {
                    break;
                }
                Thread.sleep(getSubmitReceiptDelayInterval());
            }

            if (!isRunIdValid(runIdBefore, runIdAfter)) {
                log.error(String.format( //
                        "Submit failed: errorStatus = %s, maxRunIdBefore = %s, maxRunIdAfter = %s", //
                        errorStatus, //
                        runIdBefore, //
                        runIdAfter));
                return new SubmissionReceipt(null, null, errorStatus);
            }

            log.info(String.format( //
                    "Submit %s | %s | %s | %d -> %d", //
                    getOjInfo().literal, //
                    info.remoteProblemId, //
                    info.remoteAccountId, //
                    runIdBefore, //
                    runIdAfter));
            return new SubmissionReceipt(runIdAfter.toString(), remoteAccount.getAccountId(), null);
        }

    }

    private boolean isRunIdValid(Integer runIdBefore, Integer runIdAfter) {
        return runIdAfter != null && runIdAfter >= 0 && (runIdBefore == null || runIdAfter > runIdBefore);
    }

    /**
     * Can be overridden
     * 
     * @return
     */
    protected HttpHost getHost() {
        return getOjInfo().mainHost;
    }

    /**
     * Can be overridden
     * 
     * @return
     */
    protected String getCharset() {
        return getOjInfo().defaultChaset;
    }

    protected abstract boolean needLogin();

    protected abstract Integer getMaxRunId(SubmissionInfo info, DedicatedHttpClient client, boolean submitted)
            throws Exception;

    /**
     * For some OJ (e.g. CodeForces, ZOJ), after submitting, the submission
     * won't appear in status page, so it's necessary to query multiple times
     * until it appears.
     * 
     * @return the time limit(milliseconds) Virtual Judge will try unless remote
     *         run ID is retrieved
     */
    protected long getSubmitReceiptDelay() {
        return 0;
    }

    /**
     * 
     * @return the time interval(milliseconds) Virtual Judge will try unless
     *         remote run ID is retrieved
     */
    protected long getSubmitReceiptDelayInterval() {
        return 2000;
    }

    /**
     * 
     * @param info
     * @param remoteAccount
     * @param client
     * @return error status string, if submitting succeeds, return null
     * @throws Exception
     */
    protected abstract String submitCode(SubmissionInfo info, RemoteAccount remoteAccount, DedicatedHttpClient client)
            throws Exception;

}
