package judge.remote.provider.uva;

import judge.remote.RemoteOj;
import judge.remote.shared.UVAStyleQuerier;

import org.springframework.stereotype.Component;

@Component
public class UVAQuerier extends UVAStyleQuerier {

    @Override
    public RemoteOj getOj() {
        return RemoteOj.UVA;
    }

}
