package judge.remote.status;

public interface RemoteStatusNormalizer {
	
	RemoteStatusType getStatusType(String rawStatus);

}
