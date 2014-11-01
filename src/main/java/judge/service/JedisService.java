package judge.service;

import com.google.gson.Gson;
import judge.tool.ApplicationContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Type;

@Component
@DependsOn(value = "applicationConfigPopulator")
public class JedisService {
    private final static Logger log = LoggerFactory.getLogger(JedisService.class);

    private JedisPool pool;
    private Gson gson = new Gson();

    @PostConstruct
    public void init() {
        String redisAddress = (String) ApplicationContainer.serveletContext.getAttribute("redis.address");
        String redisPortStr = (String) ApplicationContainer.serveletContext.getAttribute("redis.port");

        if (redisAddress == null || redisPortStr == null) {
            log.warn("No Redis config found! Redis is disabled.");
            return;
        }

        int redisPort = Integer.parseInt(redisPortStr);

        try {
            pool = new JedisPool(new JedisPoolConfig(), redisAddress, redisPort);
        } catch (Throwable t) {
            log.error("Redis isn't configured correctly. Redis is disabled.");
        }
    }

    @PreDestroy
    public void destroy() {
        if (pool != null) {
            pool.destroy();
        }
    }

    public <V> V execute(JedisTask<V> jedisTask) {
        if (pool == null) {
            log.error("Redis is not configured. Task is ignored.");
            return null;
        }
        try (Jedis jedis = pool.getResource()) {
            try {
                return jedisTask.execute(jedis);
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
                jedisTask.onError(t);
                throw t;
            }
        }
    }

    public void set(final String key, final Object value) {
        execute(new JedisTask<Void>() {
            @Override
            public Void execute(Jedis jedis) {
                String serializedValue = gson.toJson(value);
                jedis.set(key, serializedValue);
                return null;
            }

            @Override
            public void onError(Throwable t) {
            }
        });
    }

    public <T> T get(final String key, final Type type) {
        return execute(new JedisTask<T>() {
            @Override
            public T execute(Jedis jedis) {
                String deserializedValue = jedis.get(key);
                return gson.fromJson(deserializedValue, type);
            }

            @Override
            public void onError(Throwable t) {
            }
        });
    }

}
