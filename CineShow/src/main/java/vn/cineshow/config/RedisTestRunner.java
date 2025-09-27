package vn.cineshow.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;



import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisTestRunner implements CommandLineRunner {

    private final StringRedisTemplate redisTemplate;

    public RedisTestRunner(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) {
        redisTemplate.opsForValue().set("testKey", "Hello Redis Cloud");
        String value = redisTemplate.opsForValue().get("testKey");
        System.out.println("Redis test value = " + value);
    }
}
