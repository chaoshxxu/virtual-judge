package judge.remote.provider.scu;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class SCUInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.SCU, //
            "SCU", //
            new HttpHost("cstest.scu.edu.cn") //
    );

    static {
        INFO.defaultChaset = "GBK";
        INFO.faviconUrl = "images/remote_oj/SCU_favicon.ico";
    }

}
