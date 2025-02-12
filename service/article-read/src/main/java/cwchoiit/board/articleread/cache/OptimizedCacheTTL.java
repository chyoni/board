package cwchoiit.board.articleread.cache;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Duration;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OptimizedCacheTTL {
    private Duration logicalTimeToLive;
    private Duration physicalTimeToLive;

    public static final long PHYSICAL_TTL_DELAY_SECONDS = 5L;

    public static OptimizedCacheTTL of(long ttlSeconds) {
        OptimizedCacheTTL optimizedCacheTTL = new OptimizedCacheTTL();
        optimizedCacheTTL.logicalTimeToLive = Duration.ofSeconds(ttlSeconds);
        optimizedCacheTTL.physicalTimeToLive = optimizedCacheTTL.logicalTimeToLive.plusSeconds(PHYSICAL_TTL_DELAY_SECONDS);
        return optimizedCacheTTL;
    }
}
