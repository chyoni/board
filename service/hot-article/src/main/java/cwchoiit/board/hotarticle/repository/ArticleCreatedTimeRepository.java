package cwchoiit.board.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 이 레포지토리같은 경우 왜 필요하냐면, 특정 날짜의 인기글을 구할때, 좋아요 개수, 조회수 등 여러 기준이 있는데
 * 이 액션들은 꼭 해당 날짜에만 할 수 있는게 아니기 때문이다.
 * 만약, 좋아요 이벤트가 들어왔는데 그 이벤트에 대한 게시글이 오늘 게시글이 아니라면, 내가 오늘 게시글에 대한 인기글을 구하고 있는데 이 좋아요 이벤트를 처리할 필요가 없다.
 * 그럼 게시글이 생성된 시간을 구해야하고 그러러면 게시글 서비스에 요청을 날려봐야 하는데 그러지 말고 이 또한, 이 인기글 서비스에서 보관하고 있으면 된다.
 */
@Repository
@RequiredArgsConstructor
public class ArticleCreatedTimeRepository {
    private final StringRedisTemplate redisTemplate;

    // hot-article::article::{articleId}::created-time
    private static final String KEY_FORMAT = "hot-article::article::%s::created-time";

    public void createOrUpdate(Long articleId, LocalDateTime createdAt, Duration timeToLive) {
        redisTemplate.opsForValue().set(
                generateKey(articleId),
                String.valueOf(createdAt.toInstant(ZoneOffset.UTC).toEpochMilli()),
                timeToLive
        );
    }

    public void delete(Long articleId) {
        redisTemplate.delete(generateKey(articleId));
    }

    public LocalDateTime read(Long articleId) {
        String result = redisTemplate.opsForValue().get(generateKey(articleId));
        if (result == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(result)), ZoneOffset.UTC);
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }
}
