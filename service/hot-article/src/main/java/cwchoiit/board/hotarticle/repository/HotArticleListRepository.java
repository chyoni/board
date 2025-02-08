package cwchoiit.board.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HotArticleListRepository {
    private final StringRedisTemplate redisTemplate;

    // hot-article::list::{yyyyMMdd}
    private static final String KEY_FORMAT = "hot-article::list::%s";

    // 시간 정보를 원하는 포맷에 맞게 문자열로 변환하기 위한 DateTimeFormatter
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 계산 후 선정된 인기글을 Redis 에 저장한다.
     *
     * @param articleId  인기글 ID
     * @param time       Redis 의 Key 가 될 시간 (yyyyMMdd)
     * @param score      인기글의 점수
     * @param limit      저장할 인기글의 총 개수 (비즈니스 정책에 따라 달라지고, 나는 10개로 선정)
     * @param timeToLive 인기글은 지난 7일 동안의 인기글을 보여줄 예정이므로 이 값은 7일이 되면 된다.
     */
    public void add(Long articleId,
                    LocalDateTime time,
                    Long score,
                    Long limit,
                    Duration timeToLive) {
        log.debug("[add] add to hot article list, key : {}", time);
        redisTemplate.executePipelined((RedisCallback<?>) action -> { // executePipelined 을 사용하면, Redis 쪽으로 네트워크 통신을 한번만 하고 그 안에서 여러개의 연산을 수행할 수 있게 해준다.
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey(time);

            // Sorted Set 을 사용할 땐, 앞에 'z'가 붙은 메서드들(zXxx, ...)을 사용하면 된다.
            conn.zAdd(key, score, String.valueOf(articleId));
            conn.zRemRange(key, 0, -limit - 1); // 상위 10개만 선정하여 보관할 것이기 때문에 받은 limit(10)을 사용해서 최대 저장 범위를 설정
            conn.expire(key, timeToLive.toSeconds()); // TTL 설정
            return null; // Callback 의 리턴값은 필요없으므로 null 반환
        });
    }

    public void remove(Long articleId, LocalDateTime time) {
        redisTemplate.opsForZSet()
                .remove(generateKey(time), String.valueOf(articleId));
    }

    private String generateKey(LocalDateTime time) {
        return generateKey(TIME_FORMATTER.format(time));
    }

    private String generateKey(String dateStr) {
        return KEY_FORMAT.formatted(dateStr);
    }

    /**
     * 점수 계산 후 선정하여 보관중인 특정 날짜의 인기글을 전부 조회한다.
     *
     * @param dateStr 특정 날짜 (yyyyMMdd)
     * @return 해당 날짜에 선정된 인기글 (10건=비즈니스 정책)
     */
    public List<Long> readAll(String dateStr) {
        // opsForZSet()을 사용하면, Sorted Set 에서 조회할 수 있고, 점수가 가장 큰 인기글부터 조회하기 위해 reverseRangeWithScores 사용
        return Objects.requireNonNull(redisTemplate.opsForZSet().reverseRangeWithScores(generateKey(dateStr), 0, -1)) // 0, -1은 처음부터 끝까지를 의미
                .stream()
                .peek(tuple -> log.info("[readAll] articleId = {}, score = {}", tuple.getValue(), tuple.getScore())) // peek 은 그냥 로깅한번 하기 위함. 여기서 비즈니스 로직 수행하지 말 것
                .map(ZSetOperations.TypedTuple::getValue)
                .filter(Objects::nonNull)
                .map(Long::valueOf)
                .toList();
    }
}
