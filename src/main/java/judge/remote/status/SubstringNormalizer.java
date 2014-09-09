package judge.remote.status;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class SubstringNormalizer implements RemoteStatusNormalizer {
	
	private LinkedHashMap<String, RemoteStatusType> statusTypeMap;

	public SubstringNormalizer(Object... args) {
		this.statusTypeMap = new LinkedHashMap<String, RemoteStatusType>();
		Validate.isTrue(args.length % 2 == 0);
		for (int i = 0; i + 1 < args.length; i += 2) {
			String substring = (String) args[i];
			RemoteStatusType statusType = (RemoteStatusType) args[i + 1];
			statusTypeMap.put(substring, statusType);
		}
	}

	@Override
	public RemoteStatusType getStatusType(String rawStatus) {
		if (StringUtils.isBlank(rawStatus)) {
			return RemoteStatusType.JUDGING;
		}
		for (Entry<String, RemoteStatusType> entry : statusTypeMap.entrySet()) {
			String subStr = entry.getKey();
			if (StringUtils.containsIgnoreCase(rawStatus, subStr)) {
				return entry.getValue();
			}
		}
		return RemoteStatusType.FAILED_OTHER;
	}

}
