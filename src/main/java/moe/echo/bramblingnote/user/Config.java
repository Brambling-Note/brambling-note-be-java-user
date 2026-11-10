package moe.echo.bramblingnote.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
public class Config {
    @Value("${cache.ttl.min:3600}")
    private int minTtl;
    @Value("${cache.ttl.max:7200}")
    private int maxTtl;

    // Each entry gets its own random TTL, so keys written together do not expire together (cache avalanche).
    // Null values stay cached (the default), so a missing id does not hit the database each time (cache penetration).
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties properties) {
        Duration base = properties.getRedis().getTimeToLive() == null
                ? Duration.ZERO
                : properties.getRedis().getTimeToLive();

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl((key, value) -> base.plusSeconds(
                        ThreadLocalRandom.current().nextInt(minTtl, maxTtl + 1)));
    }
}
