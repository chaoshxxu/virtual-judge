package judge.remote.provider.lightoj;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class LightOJInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.LightOJ, //
            "LightOJ", //
            new HttpHost("lightoj.com") //
    );
    
    static {
        INFO.maxInactiveInterval = 0;
    }

}
