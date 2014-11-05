package judge.remote.provider.uva;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class UVAInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.UVA, //
            "UVA", //
            new HttpHost("uva.onlinejudge.org") //
    );
    
    static {
        INFO.faviconUrl = "images/remote_oj/UVA_favicon.ico";
        INFO._64IntIoFormat = "%lld & %llu";
    }
}
