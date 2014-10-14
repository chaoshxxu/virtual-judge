package judge.remote.provider.aizu;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class AizuInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.Aizu, //
            "Aizu", //
            new HttpHost("judge.u-aizu.ac.jp") //
    );

    static {
        INFO.faviconUrl = "images/remote_oj/Aizu_favicon.ico";
    }
    
}
