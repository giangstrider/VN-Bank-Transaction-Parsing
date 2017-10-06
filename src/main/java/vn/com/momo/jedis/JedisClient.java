package vn.com.momo.jedis;

import lombok.extern.log4j.Log4j2;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import vn.com.momo.app.AppConfig;
import vn.com.momo.app.AppUtils;


@Log4j2
public class JedisClient {
    private static JedisClient client = JedisClient.init();
    private JedisPool jedisPool;

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    private static JedisClient init() {
        try {
            client = new JedisClient(
                    AppConfig.getInstance().getJedisClient().getProperty("host", "localhost"),
                    Integer.valueOf(AppConfig.getInstance().getJedisClient().getProperty("port", "6379")),
                    Integer.valueOf(AppConfig.getInstance().getJedisClient().getProperty("timeout", "5000"))
            );

        } catch (Exception ex) {
            throw new RuntimeException("jedis init error !");
        }

        return client;
    }

    public static JedisClient getInstance() {
        if (client == null || client.jedisPool.isClosed())
            init();
        return client;
    }

    public JedisClient(String host, int port, int timeout) {
        JedisPool pool = new JedisPool(new JedisPoolConfig(), host, port, timeout);
        setJedisPool(pool);
    }

    public String get(String key) {
        try (Jedis jedis = getJedisPool().getResource()) {
            return jedis.get(key);
        } catch (Exception ex) {
            log.error("{}; key = {};", AppUtils.getFullStackTrace(ex), key);
        }

        return "";
    }

    public String set(String key, String value) {

        try (Jedis jedis = getJedisPool().getResource()) {
            return jedis.set(key, value);
        } catch (Exception ex) {
            log.error("{}; key = {};", AppUtils.getFullStackTrace(ex), key);
        }

        return "";
    }

    public long expire(String key, int seconds) {
        try (Jedis jedis = getJedisPool().getResource()) {
            return jedis.expire(key, seconds);
        } catch (Exception ex) {
            log.error("{}; key = {};", AppUtils.getFullStackTrace(ex), key);
        }

        return 0;
    }

    public boolean exists(String key) {
        try (Jedis jedis = getJedisPool().getResource()) {
            return jedis.exists(key);
        } catch (Exception ex) {
            log.error("{}; key = {};", AppUtils.getFullStackTrace(ex), key);
        }

        return false;
    }

    public void hset(String key, String field, String value, int seconds) {

        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.hset(key, field, value);
            jedis.expire(key, seconds);
        } catch (Exception ex) {
            log.error("{}; key = {};", AppUtils.getFullStackTrace(ex), key);
        }
    }

    public String hget(String key, String field) {

        try (Jedis jedis = getJedisPool().getResource()) {
            return jedis.hget(key, field);

        } catch (Exception ex) {
            log.error("{}; key = {};", AppUtils.getFullStackTrace(ex), key);
        }

        return null;
    }

    public long delPattern(String pattern) {
        log.info(pattern);
        long count = 0;
        try (Jedis jedis = getJedisPool().getResource()) {
            for (String key : jedis.keys(pattern)) {
                log.info(key);
                count = jedis.del(key);
            }
            return count;
        } catch (Exception ex) {
            log.error("{}; key = {};", AppUtils.getFullStackTrace(ex), pattern);
        }

        return 0;
    }

    public JedisClient destroyJedisPool() {
        this.getJedisPool().destroy();
        return this;
    }
}
