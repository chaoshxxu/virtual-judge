package judge.remote.crawler.common;

import judge.remote.RemoteOJAware;

public interface Crawler extends RemoteOJAware {
	
	/**
	 * 
	 * @param problemId problem id on the original OJ
	 * @return
	 * @throws Exception
	 */
	RawProblemInfo crawl(String problemId) throws Exception;

}
