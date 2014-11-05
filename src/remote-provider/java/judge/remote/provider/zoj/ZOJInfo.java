package judge.remote.provider.zoj;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class ZOJInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.ZOJ, //
            "ZOJ", //
            new HttpHost("acm.zju.edu.cn") //
    );
    
    static {
        INFO.faviconUrl = "images/remote_oj/ZOJ_favicon.ico";
        INFO._64IntIoFormat = "%lld & %llu";
    }

}
