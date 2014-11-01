package judge.service;

import redis.clients.jedis.Jedis;

public interface JedisTask<V> {

    V execute(Jedis jedis);

    void onError(Throwable t);

}
