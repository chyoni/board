package cwchoiit.board.articleread.cache;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class OptimizedCacheTest {

    @Test
    void parseDataTest() {
        parseDataTest("data", 10);
        parseDataTest(3L, 10);
        parseDataTest(3, 10);
        parseDataTest(new TestClass("hi"), 10);
    }

    @Test
    void isExpiredTest() {
        assertThat(OptimizedCache.of("data", Duration.ofDays(-30)).isExpired()).isTrue();
        assertThat(OptimizedCache.of("data", Duration.ofDays(30)).isExpired()).isFalse();
    }

    void parseDataTest(Object data, long ttlSeconds) {
        OptimizedCache optimizedCache = OptimizedCache.of(data, Duration.ofSeconds(ttlSeconds));
        log.info("[parseDataTest:18] optimizedCache: {}", optimizedCache);

        Object resolvedData = optimizedCache.parseData(data.getClass());

        log.info("[parseDataTest:22] resolvedData: {}", resolvedData);
        assertThat(resolvedData).isEqualTo(data);
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestClass {
        private String data;
    }
}