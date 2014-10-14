package judge.remote.provider.sgu;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class SGUInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.SGU, //
            "SGU", //
            new HttpHost("acm.sgu.ru") //
    );
    
    static {
        INFO.faviconUrl = "images/remote_oj/SGU_favicon.ico";
    }
}
