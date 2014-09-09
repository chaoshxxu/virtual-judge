package judge.remote.submitter.common;

import judge.remote.RemoteOjAware;
import judge.tool.Handler;

/**
 * Implementation should be stateless.
 * 
 * @author Isun
 * 
 */
public interface Submitter extends RemoteOjAware {

	/**
	 * Submit source code to remote OJ. Note that there are 3 resulting cases
	 * for handler:<br>
	 * 1. OK case: handler.handle() is called, with receipt not null;<br>
	 * 2. Fail case I: handler.handle() is called, with receipt null. It will
	 * lead submission to SUBMIT_FAILED_PERM status and not allow resubmit;<br>
	 * 3. Fail case II: handler.onError() is called. It will lead submission
	 * to SUBMIT_FAILED_TEMP status and allow resubmit;
	 * 
	 * @param info
	 * @param handler
	 *            callback
	 */
	void submitCode(SubmissionInfo info, Handler<SubmissionReceipt> handler) throws Exception;

}
