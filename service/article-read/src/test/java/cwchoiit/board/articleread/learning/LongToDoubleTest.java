package cwchoiit.board.articleread.learning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class LongToDoubleTest {
    @Test
    void longToDoubleTest() {
        long longValue = 111_111_111_111_111_111L;
        System.out.println("longValue : " + longValue); // 111111111111111111
        double doubleValue = longValue;
        System.out.println("doubleValue = " + new BigDecimal(doubleValue)); // 111111111111111104

        // 이렇게 차이가 나는 이유는, long 타입이 double 타입보다 더 큰 범위를 다룰수 있는데
        // long 타입으로 다룰 수 있는 큰 수를 double 타입으로 다룰 수 없는 경우, 값의 유실이 발생한다.
    }
}
