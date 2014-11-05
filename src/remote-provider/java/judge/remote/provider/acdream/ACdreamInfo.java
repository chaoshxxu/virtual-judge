package judge.remote.provider.acdream;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;
import org.apache.http.HttpHost;

public class ACdreamInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.ACdream, //
            "ACdream", //
            new HttpHost("acdream.info") //
    );

    static {
        INFO.faviconUrl = "images/remote_oj/ACdream_favicon.ico";
        INFO._64IntIoFormat = "%lld & %llu";
    }

}
