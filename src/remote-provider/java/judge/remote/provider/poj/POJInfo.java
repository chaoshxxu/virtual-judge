package judge.remote.provider.poj;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class POJInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.POJ, //
            "POJ", //
            new HttpHost("poj.org") //
    );
    
    static {
        INFO.faviconUrl = "images/remote_oj/poj.ico";
        INFO._64IntIoFormat = "%lld & %llu";
    }

}
