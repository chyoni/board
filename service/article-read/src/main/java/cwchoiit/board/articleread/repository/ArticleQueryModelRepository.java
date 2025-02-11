package cwchoiit.board.articleread.repository;

import cwchoiit.board.common.dataserializer.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ArticleQueryModelRepository {
    private final StringRedisTemplate redisTemplate;

    // article-read::article::{articleId}
    private static final String KEY_FORMAT = "article-read::article::%s";

    public void create(ArticleQueryModel articleQueryModel, Duration timeToLive) {
        redisTemplate.opsForValue()
                .set(
                        generateKey(articleQueryModel),
                        Objects.requireNonNull(DataSerializer.serialize(articleQueryModel)),
                        timeToLive
                );
    }

    public void update(ArticleQueryModel articleQueryModel) {
        redisTemplate.opsForValue()
                .setIfPresent(
                        generateKey(articleQueryModel),
                        Objects.requireNonNull(DataSerializer.serialize(articleQueryModel))
                );
    }

    public void delete(Long articleId) {
        redisTemplate.delete(generateKey(articleId));
    }

    public Optional<ArticleQueryModel> read(Long articleId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(generateKey(articleId)))
                .map(json -> DataSerializer.deserialize(json, ArticleQueryModel.class));
    }

    private String generateKey(ArticleQueryModel articleQueryModel) {
        return generateKey(articleQueryModel.getArticleId());
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }

    public Map<Long, ArticleQueryModel> readAll(List<Long> articleIds) {
        List<String> keys = articleIds.stream()
                .map(this::generateKey)
                .toList();

        return Objects.requireNonNull(redisTemplate.opsForValue().multiGet(keys))
                .stream()
                .filter(Objects::nonNull)
                .map(json -> DataSerializer.deserialize(json, ArticleQueryModel.class))
                .collect(Collectors.toMap(Objects.requireNonNull(ArticleQueryModel::getArticleId), Function.identity()));
    }
}
