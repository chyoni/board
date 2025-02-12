package cwchoiit.board.articleread.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;

/**
 * {@code @Cacheable} 애노테이션으로 캐싱을 사용하기 위해 만든 설정 클래스
 * 지금은, 분산락 개념을 도입한 캐시 최적화를 사용하고 있어서 이 설정과 {@code @Cacheable} 사용하지 않음
 */
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Redis 에서 캐시로 저장할 cacheName 을 지정, 나는 게시글 조회수를 캐싱할 것이기 때문에 "articleViewCount" 를 지정했고, 원한다면 더 추가할 수 있음 Map 안에
        Map<String, RedisCacheConfiguration> articleViewCountCache =
                Map.of("articleViewCount", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(1)));

        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations(articleViewCountCache)
                .build();
    }
}
