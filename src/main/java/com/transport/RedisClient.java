package com.transport;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

public class RedisClient {
    private Jedis jedis;

    public RedisClient(String host, int port, String password) {
        jedis = new Jedis(host, port);
        jedis.auth(password);
    }

    public void set(String key, String value) {
        jedis.set(key, value);
    }

    public String get(String key) {
        return jedis.get(key);
    }

    public void setHash(String key, Map<String, String> hash) {
        jedis.hset(key, hash);
    }

    public Map<String, String> getHash(String key) {
        return jedis.hgetAll(key);
    }

    public void setList(String key, String... values) {
        jedis.lpush(key, values);
    }

    public List<String> getList(String key) {
        return jedis.lrange(key, 0, -1);
    }

    public void close() {
        jedis.close();
    }
}
