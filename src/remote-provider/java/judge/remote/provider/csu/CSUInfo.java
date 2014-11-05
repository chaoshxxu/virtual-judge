package judge.remote.provider.csu;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class CSUInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.CSU, //
            "CSU", //
            new HttpHost("acm.csu.edu.cn") //
    );
    
    static {
        INFO.faviconUrl = "images/remote_oj/CSU_favicon.ico";
        INFO._64IntIoFormat = "%lld & %llu";
    }
}
