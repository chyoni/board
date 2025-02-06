package cwchoiit.board.common.outboxmessagerelay;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 살아 있는 애플리케이션들을 관리할 Coordinator.
 */
@Component
@RequiredArgsConstructor
public class MessageRelayCoordinator {
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 애플리케이션 고유 ID.
     * 예를 들어, Article 서비스가 10개가 띄워져있다면, 10개 서비스 각각은 다 Article 서비스지만 고유 ID는 달라야 한다.
     * 그 값을 정의한 값.
     */
    private final String APP_ID = UUID.randomUUID().toString();
    /**
     * 3초마다 ping 날림
     */
    private final int PING_INTERNAL_SECONDS = 3;
    /**
     * 3번을 날렸는데 실패한 경우 애플리케이션이 죽었다고 판단
     */
    private final int PING_FAILURE_THRESHOLD = 3;

    /**
     * AssignedShard 객체를 생성한다.
     * @return {@link AssignedShard}
     */
    public AssignedShard assignShards() {
        return AssignedShard.of(APP_ID, findAppIds(), MessageRelayConstants.SHARD_COUNT);
    }

    private List<String> findAppIds() {
        // redisTemplate.opsForZSet().reverseRange(generateKey(), 0, -1) 이렇게 하면, Sorted Set 에 들어있는 generateKey()에 해당하는 모든 값들을 다 찾는 것 (0, -1)이 모든 범위를 말함
        return Objects.requireNonNull(redisTemplate.opsForZSet().reverseRange(generateKey(), 0, -1))
                .stream()
                .sorted()
                .toList();
    }

    @Scheduled(fixedDelay = PING_INTERNAL_SECONDS, timeUnit = TimeUnit.SECONDS)
    public void ping() {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey();

            // 3초마다 ping 을 날려서, 이 APP_ID 의 score 를 현재 시간으로 변경한다.
            conn.zAdd(key, Instant.now().toEpochMilli(), APP_ID);

            // score 가 9초가 지난 애플리케이션은 zSet 에서 삭제한다.
            conn.zRemRangeByScore(
                    key,
                    Double.NEGATIVE_INFINITY,
                    Instant.now().minusSeconds(PING_INTERNAL_SECONDS * PING_FAILURE_THRESHOLD).toEpochMilli()
            );
            return null;
        });
    }

    /**
     * 애플리케이션이 종료될 경우엔 그냥 자기 자신을 zSet 에서 제거.
     */
    @PreDestroy
    public void leave() {
        redisTemplate.opsForZSet().remove(generateKey(), APP_ID);
    }

    /**
     * Article 서비스라면, applicationName 은 board-article 이고 그 값을 사용해서 키를 만들면 이 키에 해당하는 여러개의 Article 서비스가
     * Sorted Set 에 저장될 것.
     * @return Redis key
     */
    private String generateKey() {
        return "message-relay-coordinator::app-list::%s".formatted(applicationName);
    }
}
