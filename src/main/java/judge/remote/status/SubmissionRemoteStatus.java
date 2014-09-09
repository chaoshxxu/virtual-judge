package judge.remote.status;

import java.util.Date;

public class SubmissionRemoteStatus {
	
	public RemoteStatusType statusType;
	
	public String rawStatus;
	
	/**
	 * millisecond
	 */
	public int executionTime;
	
	/**
	 * KiloBytes
	 */
	public int executionMemory;
	
	public String compilationErrorInfo;
	
	public int failCase = -1;
	
	public Date queryTime = new Date();

}
