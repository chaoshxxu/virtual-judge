package judge.remote.submitter.common;

import java.util.HashMap;
import java.util.List;

import judge.remote.RemoteOj;
import judge.tool.SpringBean;
import judge.tool.Tools;

public class SubmittersHolder {

	private static HashMap<RemoteOj, Submitter> submitters = new HashMap<RemoteOj, Submitter>();

	public static Submitter getSubmitter(RemoteOj remoteOj) {
		if (!submitters.containsKey(remoteOj)) {
			synchronized (submitters) {
				if (!submitters.containsKey(remoteOj)) {
					try {
						List<Class<? extends Submitter>> submitterClasses = Tools.findSubClasses("judge/remote/submitter", Submitter.class);
						for (Class<? extends Submitter> submitterClass : submitterClasses) {
							Submitter submitter = SpringBean.getBean(submitterClass);
							submitters.put(submitter.getOj(), submitter);
						}
					} catch (Throwable t) {
						t.printStackTrace();
						System.exit(-1);
					}
				}
			}
		}
		return submitters.get(remoteOj);
	}

}
