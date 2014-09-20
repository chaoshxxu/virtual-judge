package judge.remote.loginer.common;

import judge.remote.RemoteOjAware;
import judge.remote.account.RemoteAccount;

/**
 * Implementation should be stateless.
 * @author Isun
 *
 */
public interface Loginer extends RemoteOjAware {

    void login(RemoteAccount account) throws Exception;

}
