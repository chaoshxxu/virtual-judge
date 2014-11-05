package judge.remote.provider.uvalive;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class UVALiveInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.UVALive, //
            "UVALive", //
            new HttpHost("icpcarchive.ecs.baylor.edu", 443, "https") //
    );
    
    static {
        INFO.faviconUrl = "images/remote_oj/UVA_favicon.ico";
        INFO._64IntIoFormat = "%lld & %llu";
    }

}
