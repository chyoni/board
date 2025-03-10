package cwchoiit.board.like.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ArticleLikeRepositoryTest {

    @Test
    void countDownLatch() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(5);
        log.info("[countDownLatch:14] count: {}", countDownLatch.getCount());
        countDownLatch.countDown();
        log.info("[countDownLatch:18] count: {}", countDownLatch.getCount());
        countDownLatch.await();
    }

}