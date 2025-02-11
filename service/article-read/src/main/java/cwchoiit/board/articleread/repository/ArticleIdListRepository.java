package cwchoiit.board.articleread.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ArticleIdListRepository {
    private final StringRedisTemplate redisTemplate;

    // article-read::board::{boardId}::article-list
    private static final String KEY_FORMAT = "article-read::board::%s::article-list";

    /**
     * {@code Redis}에 최신글 최대 1000개까지만 캐싱을 하는 전략을 사용할 것이고, 그래서 최신글을 {@code Redis}에 저장하는 메서드.
     * 그런데, 우리는 지금 기본키를 {@code Snowflake} 전략을 사용하고 있다. 이 기본키 전략은 64비트를 사용하여 기본키를 나타내는데 {@code Redis}에 저장되는 값인
     * {@code score}는 타입이 {@code double} 타입이다. {@code double} 타입은 {@code long} 타입이 다룰 수 있는 매우 큰 숫자를 다루지 못한다.
     * 따라서 목록이 꼬일 수 있기 때문에, {@code score}는 모두 0으로 동일하게 설정하고 {@code value}값을 게시글 ID로 설정하면 동일한 {@code score}의 경우,
     * {@code value}로 정렬을 할 수 있기 때문에 이렇게 사용한다.
     * @param boardId boardId
     * @param articleId articleId
     * @param limit limit (우리는 1000개 고정)
     */
    public void add(Long boardId, Long articleId, Long limit) {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey(boardId);
            conn.zAdd(key, 0, toPaddedString(articleId)); // score 가 동일한 경우, value 값으로 정렬이 된다.
            conn.zRemRange(key, 0, -limit - 1); // 상위 limit 개수까지만 보관 예를들어, limit = 1000 이면 0부터 999개까지
            return null;
        });
    }

    public void delete(Long boardId, Long articleId) {
        redisTemplate.opsForZSet().remove(generateKey(boardId), toPaddedString(articleId));
    }

    public List<Long> readAll(Long boardId, Long offset, Long limit) {
        return Objects
                .requireNonNull(redisTemplate.opsForZSet().reverseRange(generateKey(boardId), offset, offset + limit - 1))
                .stream()
                .map(Long::valueOf)
                .toList();
    }

    /**
     * 무한 스크롤 방식의 게시글 목록조회를 Redis 캐시값으로 반환한다.
     * 코드를 보면, {@code reverseRangeByLex}를 사용하고 있는데, 이는 {@code zSet}에서 {@code score}가 다 동일한 경우엔 {@code value}값으로 정렬이 되는데,
     * {@code value} 값으로 내림차순 정렬된 상태로 조회를 하려면 {@code reverseRangeByLex} 를 사용하면 되기 때문이다.
     * 또한, 데이터가 만약, 6 5 4 3 2 1 이렇게 정렬되어 있을때, {@code Range.unbounded()} 를 사용하면 처음부터 {@code limit} 만큼 가져오는 것이고
     * {@code Range.leftUnbounded(Range.Bound.exclusive(...))}를 사용하면 ...으로 들어간 값 이후부터 조회하는 것이라고 생각하면 된다.
     * @param boardId boardId
     * @param lastArticleId lastArticleId
     * @param limit limit
     * @return 게시글 목록 리스트
     */
    public List<Long> readAllInfinite(Long boardId, Long lastArticleId, Long limit) {
        return Objects
                .requireNonNull(redisTemplate.opsForZSet().reverseRangeByLex(
                        generateKey(boardId),
                        lastArticleId == null ?
                                Range.unbounded() :
                                Range.leftUnbounded(Range.Bound.exclusive(toPaddedString(lastArticleId))),
                        Limit.limit().count(limit.intValue())))
                .stream()
                .map(Long::valueOf)
                .toList();
    }

    private String toPaddedString(Long articleId) {
        // articleId : 1234 -> 0000000000000001234 (19자리를 맞추기 위함, 앞은 0으로 다 채움)
        return "%019d".formatted(articleId);
    }

    private String generateKey(Long boardId) {
        return KEY_FORMAT.formatted(boardId);
    }
}
