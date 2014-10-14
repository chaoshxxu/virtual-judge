package judge.remote.provider.fzu;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class FZUInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.FZU, //
            "FZU", //
            new HttpHost("acm.fzu.edu.cn") //
    );
    
    static {
        INFO.faviconUrl = "images/remote_oj/FZU_favicon.gif";
    }
}
