package judge.remote.provider.ztrening;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class ZTreningInfo {
    
    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.ZTrening, //
            "Z-Trening", //
            new HttpHost("www.z-trening.com") //
    );

    static {
        INFO._64IntIoFormat = "%lld & %llu";
    }
    
}
