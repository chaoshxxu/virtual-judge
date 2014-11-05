package judge.remote.provider.hust;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class HUSTInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.HUST, //
            "HUST", //
            new HttpHost("acm.hust.edu.cn") //
    );
    
    static {
        INFO.faviconUrl = "images/remote_oj/HUST_icon.jpg";
        INFO._64IntIoFormat = "%lld & %llu";
    }
}
