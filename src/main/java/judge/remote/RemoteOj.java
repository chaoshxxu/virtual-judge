package judge.remote;

import org.apache.http.HttpHost;

public enum RemoteOj {

    Aizu( //
            "Aizu", //
            new HttpHost("judge.u-aizu.ac.jp") //
    ),

    CodeForces( //
            "CodeForces", //
            new HttpHost("codeforces.com") //
    ),

    CSU( //
            "CSU", //
            new HttpHost("acm.csu.edu.cn") //
    ),

    FZU( //
            "FZU", //
            new HttpHost("acm.fzu.edu.cn") //
    ),

    HDU( //
            "HDU", //
            new HttpHost("acm.hdu.edu.cn"), //
            "gb2312", //
            60000L
    ),

    HUST( //
            "HUST", //
            new HttpHost("acm.hust.edu.cn") //
    ),

    HYSBZ( //
            "HYSBZ", //
            new HttpHost("www.lydsy.com") //
    ),

    LightOJ( //
            "LightOJ", //
            new HttpHost("lightoj.com"), //
            null, //
            0 //
    ),

    NBUT( //
            "NBUT", //
            new HttpHost("ac.nbutoj.com") //
    ),

    POJ( //
            "POJ", //
            new HttpHost("poj.org") //
    ),

    SCU( //
            "SCU", //
            new HttpHost("cstest.scu.edu.cn"), //
            "GBK"
    ),

    SGU( //
            "SGU", //
            new HttpHost("acm.sgu.ru") //
    ),

    SPOJ( //
            "SPOJ", //
            new HttpHost("www.spoj.com"), //
            "ISO-8859-1" //
    ),

    UESTC( //
            "UESTC", //
            new HttpHost("acm.uestc.edu.cn") //
    ),

    UESTCOld( //
            "UESTC-old", //
            new HttpHost("acm.uestc.edu.cn") //
    ),

    URAL( //
            "URAL", //
            new HttpHost("acm.timus.ru") //
    ),

    UVA( //
            "UVA", //
            new HttpHost("uva.onlinejudge.org") //
    ),

    UVALive( //
            "UVALive", //
            new HttpHost("icpcarchive.ecs.baylor.edu", 443, "https") //
    ),

    ZOJ( //
            "ZOJ", //
            new HttpHost("acm.zju.edu.cn") //
    ),

    ZTrening( //
            "Z-Trening", //
            new HttpHost("www.google.com") //
    ),

    ;
    
    ////////////////////////////////////////////////////////////////
    public String literal;
    public HttpHost mainHost;
    public String defaultChaset;
    
    /**
     * In milliseconds
     */
    public long maxInactiveInterval;
    ////////////////////////////////////////////////////////////////

    
    RemoteOj(String literal, HttpHost mainHost) {
        this(literal, mainHost, "UTF-8");
    }

    RemoteOj(String literal, HttpHost mainHost, String defaultChaset) {
        this(literal, mainHost, defaultChaset, 300000L);
    }

    RemoteOj(String literal, HttpHost mainHost, String defaultChaset, long maxInactiveInterval) {
        this.literal = literal;
        this.mainHost = mainHost;
        this.defaultChaset = defaultChaset == null ? "UTF-8" : defaultChaset;
        this.maxInactiveInterval = maxInactiveInterval;
    }

    @Override
    public String toString() {
        return literal;
    }
}
