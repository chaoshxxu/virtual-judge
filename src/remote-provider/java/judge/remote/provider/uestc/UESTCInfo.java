package judge.remote.provider.uestc;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class UESTCInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.UESTC, //
            "UESTC", //
            new HttpHost("acm.uestc.edu.cn") //
    );
    
    static {
        INFO.faviconUrl = "images/remote_oj/UESTC_favicon.png";
    }
}
