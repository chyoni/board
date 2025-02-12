package cwchoiit.board.articleread.cache;

import cwchoiit.board.common.dataserializer.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 이 클래스를 통해, 캐시를 처리하고 캐시가 없는 경우 원본 데이터를 가져와서 캐시에 적재하는 등의 작업을 수행
 */
@Component
@RequiredArgsConstructor
public class OptimizedCacheManager {
    private final StringRedisTemplate redisTemplate;
    private final OptimizedCacheLockProvider optimizedCacheLockProvider;

    private static final String DELIMITER = "::";

    /**
     * Redis 에서 특정키로 된 캐시를 가져오고, 없다면 원본 데이터를 가져와 캐시에 적재하고 클라이언트에게 반환하고,
     * Logical TTL, Physical TTL 을 확인하여 캐시를 적절하게 반환한다.
     * 파라미터로 받는 {@code type}, {@code args}는 키를 만드는 데 사용된다. 예를 들어,
     * type = "articleViewCount", args = [Long articleId(1234)] 라면,
     * articleViewCount::1234 이렇게 키가 생긴다.
     * @param type Redis 키로 저장될 type
     * @param ttlSeconds ttl
     * @param args Redis 키로 저장될 args
     * @param returnType 캐시의 반환 타입
     * @param originDataSupplier 원본 데이터를 가져오는 람다
     * @return 캐시 데이터
     * @throws Throwable 원본 데이터를 가져오는 도중 오류가 발생한 경우
     */
    public Object process(String type,
                          long ttlSeconds,
                          Object[] args,
                          Class<?> returnType,
                          OptimizedCacheOriginDataSupplier<?> originDataSupplier) throws Throwable {
        String key = generateKey(type, args);

        String cachedData = redisTemplate.opsForValue().get(key); // 캐시된 데이터
        if (cachedData == null) {
            return refresh(originDataSupplier, key, ttlSeconds); // 캐시 데이터가 없다면(=physical TTL 도 만료), 원본 데이터를 가져온다.
        }

        OptimizedCache optimizedCache = DataSerializer.deserialize(cachedData, OptimizedCache.class);
        if (optimizedCache == null) { // deserialize 에 문제가 있는 경우
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        if (!optimizedCache.isExpired()) { // Logical TTL 이 만료되지 않았다면
            return optimizedCache.parseData(returnType); // 캐시 데이터를 그대로 반환
        }

        // Logical TTL 이 만료된 상태

        // Lock 획득 시도
        if (!optimizedCacheLockProvider.lock(key)) {
            return optimizedCache.parseData(returnType); // Lock 획득에 실패한 경우, 그냥 본인의 캐시를 반환, Physical TTL 은 만료되지 않았으므로
        }

        // 락을 획득한 스레드만 접근할 수 있는 라인

        try {
            // Logical TTL 이 만료됐으니, 원본 데이터를 다시 획득하여 캐시에 적재할 의무 수행
            return refresh(originDataSupplier, key, ttlSeconds);
        } finally {
            optimizedCacheLockProvider.unlock(key);
        }
    }

    /**
     * 캐시의 원본 데이터를 가져오고, 다시 캐시 데이터로 적재한다.
     * @param originDataSupplier 원본 데이터를 가져오는 방법을 가진 함수형 인터페이스
     * @param key Redis key
     * @param ttlSeconds TTL
     * @return 원본 데이터
     * @throws Throwable 원본 데이터를 가져오는 중에 어떠한 에러라도 발생한 경우
     */
    private Object refresh(OptimizedCacheOriginDataSupplier<?> originDataSupplier,
                           String key,
                           long ttlSeconds) throws Throwable {
        Object result = originDataSupplier.get();

        OptimizedCacheTTL optimizedCacheTTL = OptimizedCacheTTL.of(ttlSeconds);
        OptimizedCache optimizedCache = OptimizedCache.of(result, optimizedCacheTTL.getLogicalTimeToLive());

        redisTemplate.opsForValue().set(
                key,
                Objects.requireNonNull(DataSerializer.serialize(optimizedCache)),
                optimizedCacheTTL.getPhysicalTimeToLive()
        );

        return result;
    }

    private String generateKey(String prefix, Object[] args) {
        return prefix + DELIMITER +
                Arrays.stream(args)
                        .map(String::valueOf)
                        .collect(Collectors.joining(DELIMITER));
    }
}
