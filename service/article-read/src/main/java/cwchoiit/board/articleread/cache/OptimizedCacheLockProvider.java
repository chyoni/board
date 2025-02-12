package cwchoiit.board.articleread.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 캐시 갱신할 때 동시성 문제를 해결하기 위해, 분산락을 사용한다.
 * 그 락을 점유하거나, 점유하지 못하거나를 판단하기 위해 락을 {@code Redis}에 저장하기 위한 클래스
 */
@Component
@RequiredArgsConstructor
public class OptimizedCacheLockProvider {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "optimized-cache-lock::";
    private static final Duration LOCK_TTL = Duration.ofSeconds(3);

    public boolean lock(String key) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue()
                        .setIfAbsent(generateLockKey(key), "", LOCK_TTL) // 동일한 키가 이미 있으면 set 하지 않고 false 반환 = Lock 을 누군가 이미 점유중
        );
    }

    public void unlock(String key) {
        redisTemplate.delete(generateLockKey(key));
    }

    private String generateLockKey(String key) {
        return KEY_PREFIX + key;
    }
}
