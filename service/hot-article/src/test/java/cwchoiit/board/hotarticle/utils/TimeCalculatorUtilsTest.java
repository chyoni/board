package cwchoiit.board.hotarticle.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class TimeCalculatorUtilsTest {

    @Test
    void test() {
        Duration duration = TimeCalculatorUtils.calculateDurationToMidnight();
        log.info("duration.getSeconds: {}", duration.getSeconds() / 60);
    }
}