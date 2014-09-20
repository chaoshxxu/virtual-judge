package judge.remote.status;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class SubstringNormalizer implements RemoteStatusNormalizer {
    
    @SuppressWarnings("serial")
    private static LinkedHashMap<String, RemoteStatusType> baseStatusTypeMap = new LinkedHashMap<String, RemoteStatusType>(){{
        put("Wait", RemoteStatusType.QUEUEING);
        put("Pend", RemoteStatusType.QUEUEING);
        put("Queuing", RemoteStatusType.QUEUEING);
        put("Queue", RemoteStatusType.QUEUEING);

        put("Compiling", RemoteStatusType.COMPILING);

        put("Accepted", RemoteStatusType.AC);
        put("Presentation Error", RemoteStatusType.PE);
        put("Format Error", RemoteStatusType.PE);

        put("Compile Error", RemoteStatusType.CE);
        put("Compilation Error", RemoteStatusType.CE);

        put("Wrong Answer", RemoteStatusType.WA);
        put("Time Limit Exceed", RemoteStatusType.TLE);
        put("Memory Limit Exceed", RemoteStatusType.MLE);
        put("Output Limit Exceed", RemoteStatusType.OLE);
        put("Runtime Error", RemoteStatusType.RE);
        put("Segmentation Fault", RemoteStatusType.RE);
        put("Floating Point Error", RemoteStatusType.RE);
        put("Crash", RemoteStatusType.RE);

        put("Running", RemoteStatusType.JUDGING);
        put("ing", RemoteStatusType.JUDGING);
    }};
    public static final SubstringNormalizer DEFAULT = new SubstringNormalizer();
    private LinkedHashMap<String, RemoteStatusType> statusTypeMap;

    
    public SubstringNormalizer(Object... args) {
        this.statusTypeMap = new LinkedHashMap<String, RemoteStatusType>(baseStatusTypeMap);
        Validate.isTrue(args.length % 2 == 0);
        for (int i = 0; i + 1 < args.length; i += 2) {
            String rawStatus = (String) args[i];
            RemoteStatusType statusType = (RemoteStatusType) args[i + 1];
            if (statusType == null) {
                statusTypeMap.remove(rawStatus);
            } else {
                statusTypeMap.put(rawStatus, statusType);
            }
        }
    }

    @Override
    public RemoteStatusType getStatusType(String rawStatus) {
        for (Entry<String, RemoteStatusType> entry : statusTypeMap.entrySet()) {
            String subStr = entry.getKey();
            if (StringUtils.containsIgnoreCase(rawStatus, subStr)) {
                return entry.getValue();
            }
        }
        return RemoteStatusType.FAILED_OTHER;
    }

}
