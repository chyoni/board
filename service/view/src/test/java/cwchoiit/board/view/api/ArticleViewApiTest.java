package cwchoiit.board.view.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ArticleViewApiTest {
    RestClient restClient = RestClient.create("http://localhost:9003");

    @Test
    void viewTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch countDownLatch = new CountDownLatch(10000);

        for (int i = 0; i < 10000; i++) {
            executorService.submit(() -> {
                restClient.post()
                        .uri("/v1/article-view/articles/{articleId}/users/{userId}", 3L, 1L)
                        .retrieve()
                        .body(Long.class);
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();

        Long count = restClient.get()
                .uri("/v1/article-view/articles/{articleId}/count", 3L)
                .retrieve()
                .body(Long.class);

        log.info("count: {}", count);

        executorService.close();
    }
}
