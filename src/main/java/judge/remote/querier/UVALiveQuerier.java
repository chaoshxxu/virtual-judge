package judge.remote.querier;

import org.springframework.stereotype.Component;

import judge.remote.RemoteOj;

@Component
public class UVALiveQuerier extends UVAStyleQuerier {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.UVALive;
    }

}
