package cwchoiit.board.articleread.cache;

import cwchoiit.board.common.dataserializer.DataSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * {@code Redis}에 적재될 캐시 데이터 클래스
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OptimizedCache {
    private String data;
    private LocalDateTime expiredAt; // Logical TTL 에 의해 만료되는 시간

    public static OptimizedCache of(Object data, Duration timeToLive) {
        OptimizedCache optimizedCache = new OptimizedCache();
        optimizedCache.data = DataSerializer.serialize(data);
        optimizedCache.expiredAt = LocalDateTime.now().plus(timeToLive);
        return optimizedCache;
    }

    /**
     * Logical TTL 이 만료되었는지 확인
     *
     * @return 만료되었다면 {@code true}, 그렇지 않으면 {@code false}
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public <T> T parseData(Class<T> dataType) {
        return DataSerializer.deserialize(data, dataType);
    }
}
