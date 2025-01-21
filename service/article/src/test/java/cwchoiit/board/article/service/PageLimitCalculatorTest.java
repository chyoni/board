package cwchoiit.board.article.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageLimitCalculatorTest {

    @Test
    void calculatePageLimitTest() {
        calculatePageLimit(1L, 30L, 10L, 301L);
        calculatePageLimit(7L, 30L, 10L, 301L);
        calculatePageLimit(10L, 30L, 10L, 301L);
        calculatePageLimit(11L, 30L, 10L, 601L);
        calculatePageLimit(18L, 30L, 10L, 601L);
    }

    void calculatePageLimit(Long page, Long pageSize, Long movablePageCount, Long expected) {
        Long result = PageLimitCalculator.calculatePageLimit(page, pageSize, movablePageCount);
        assertThat(result).isEqualTo(expected);
    }
}