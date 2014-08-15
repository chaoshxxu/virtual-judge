package judge.remote.loginer.common;

import judge.account.RemoteAccount;
import judge.remote.RemoteOJAware;

public interface Loginer extends RemoteOJAware {

	void login(RemoteAccount account);

}
