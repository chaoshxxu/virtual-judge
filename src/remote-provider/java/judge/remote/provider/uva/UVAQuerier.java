package judge.remote.provider.uva;

import judge.remote.RemoteOjInfo;
import judge.remote.shared.UVAStyleQuerier;

import org.springframework.stereotype.Component;

@Component
public class UVAQuerier extends UVAStyleQuerier {

    @Override
    public RemoteOjInfo getOjInfo() {
        return UVAInfo.INFO;
    }

}
