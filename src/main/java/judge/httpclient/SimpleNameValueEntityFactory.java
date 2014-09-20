package judge.httpclient;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

public class SimpleNameValueEntityFactory {

    /**
     * 
     * @param keyValue if size is odd, the last one is charset
     * @return
     */
    public static UrlEncodedFormEntity create(String... keyValues) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            nvps.add(new BasicNameValuePair(keyValues[i], keyValues[i + 1]));
        }
        String charset = keyValues.length % 2 == 1 ? keyValues[keyValues.length - 1] : "UTF-8";
        return new UrlEncodedFormEntity(nvps, Charset.forName(charset));
    }
    
}
