package judge.remote.provider.uvalive;

import judge.remote.RemoteOj;
import judge.remote.shared.UVAStyleQuerier;

import org.springframework.stereotype.Component;

@Component
public class UVALiveQuerier extends UVAStyleQuerier {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.UVALive;
    }

}
