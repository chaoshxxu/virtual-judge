package judge.remote.provider.codeforces;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class CodeForcesInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.CodeForces, //
            "CodeForces", //
            new HttpHost("codeforces.com") //
    );
    
    static {
        INFO.faviconUrl = "images/remote_oj/CodeForces_favicon.png";
    }
}
