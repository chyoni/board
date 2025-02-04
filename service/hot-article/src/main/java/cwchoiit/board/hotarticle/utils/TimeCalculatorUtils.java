package cwchoiit.board.hotarticle.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class TimeCalculatorUtils {

    public static Duration calculateDurationToMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.plusDays(1).with(LocalTime.MIDNIGHT); // 다음날 00시 00분을 의미
        return Duration.between(now, midnight); // 현재 시간부터 다음날 00시 00분까지 얼마나 남았는지
    }
}
