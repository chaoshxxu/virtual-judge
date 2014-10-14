package judge.remote.provider.uvalive;

import judge.remote.RemoteOjInfo;
import judge.remote.shared.UVAStyleQuerier;

import org.springframework.stereotype.Component;

@Component
public class UVALiveQuerier extends UVAStyleQuerier {

    @Override
    public RemoteOjInfo getOjInfo() {
        return UVALiveInfo.INFO;
    }

}
