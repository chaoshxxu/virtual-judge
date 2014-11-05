package judge.remote.provider.uestc_old;

import judge.remote.RemoteOj;
import judge.remote.RemoteOjInfo;

import org.apache.http.HttpHost;

public class UESTCOldInfo {

    public static final RemoteOjInfo INFO = new RemoteOjInfo( //
            RemoteOj.UESTCOld, //
            "UESTC-old", //
            new HttpHost("acm.uestc.edu.cn") //
    );

    static {
        INFO._64IntIoFormat = "%lld & %llu";
    }

}
