package cwchoiit.board.articleread.client;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class ViewClientTest {

    @Autowired
    ViewClient viewClient;

    @Test
    void readCacheableTest() throws InterruptedException {
        viewClient.count(1L);
        viewClient.count(1L);
        viewClient.count(1L);

        Thread.sleep(3000);
        viewClient.count(1L);
    }

    @Test
    void readCacheableMultiThreadTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        viewClient.count(1L);

        for (int i = 0; i < 5; i++) {
            CountDownLatch countDownLatch = new CountDownLatch(5);
            for (int j = 0; j < 5; j++) {
                executorService.submit(() -> {
                    viewClient.count(1L);
                    countDownLatch.countDown();
                });
            }
            countDownLatch.await();
            Thread.sleep(2000);
            log.info("[readCacheableMultiThreadTest:47] cache expired");
        }

        executorService.shutdown();
        executorService.close();
    }
}