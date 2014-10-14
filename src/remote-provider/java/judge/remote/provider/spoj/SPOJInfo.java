package judge.remote.provider.spoj;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class SPOJInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.SPOJ, //
            "SPOJ", //
            new HttpHost("www.spoj.com") //
    );

    static {
        INFO.defaultChaset = "ISO-8859-1";
        INFO.faviconUrl = "images/remote_oj/SPOJ_favicon.png";
    }

}
