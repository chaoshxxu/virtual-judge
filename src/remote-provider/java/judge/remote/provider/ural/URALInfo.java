package judge.remote.provider.ural;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class URALInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.URAL, //
            "URAL", //
            new HttpHost("acm.timus.ru") //
    );
    
    static {
        INFO.faviconUrl = "images/remote_oj/URAL_favicon.ico";
    }

}
