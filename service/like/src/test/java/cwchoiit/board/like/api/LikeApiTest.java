package cwchoiit.board.like.api;

import cwchoiit.board.like.service.response.ArticleLikeResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class LikeApiTest {
    RestClient restClient = RestClient.create("http://localhost:9002");

    @Test
    void likeAndUnlikeTest() {
        Long articleId = 9999L;

        like(articleId, 1L, "pessimistic-lock-1");
        like(articleId, 2L, "pessimistic-lock-1");
        like(articleId, 3L, "pessimistic-lock-1");

        ArticleLikeResponse response1 = read(articleId, 1L);
        ArticleLikeResponse response2 = read(articleId, 2L);
        ArticleLikeResponse response3 = read(articleId, 3L);

        assertThat(response1).isNotNull();
        assertThat(response2).isNotNull();
        assertThat(response3).isNotNull();

        unlike(articleId, 1L);
        unlike(articleId, 2L);
        unlike(articleId, 3L);

        assertThatThrownBy(() -> read(articleId, 1L)).isInstanceOf(Exception.class);
        assertThatThrownBy(() -> read(articleId, 2L)).isInstanceOf(Exception.class);
        assertThatThrownBy(() -> read(articleId, 3L)).isInstanceOf(Exception.class);

    }

    void like(Long articleId, Long userId, String lockType) {
        restClient.post()
                .uri("/v1/article-like/articles/{articleId}/users/{userId}/{lockType}", articleId, userId, lockType)
                .retrieve()
                .toBodilessEntity();
    }

    void unlike(Long articleId, Long userId) {
        restClient.delete()
                .uri("/v1/article-like/articles/{articleId}/users/{userId}", articleId, userId)
                .retrieve()
                .toBodilessEntity();
    }

    ArticleLikeResponse read(Long articleId, Long userId) {
        return restClient.get()
                .uri("/v1/article-like/articles/{articleId}/users/{userId}", articleId, userId)
                .retrieve()
                .body(ArticleLikeResponse.class);
    }

    @Test
    void likePerformanceTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        likePerformanceTest(executorService, 4444L, "pessimistic-lock-1");
        likePerformanceTest(executorService, 5555L, "pessimistic-lock-2");
        likePerformanceTest(executorService, 6666L, "optimistic-lock");
    }

    void likePerformanceTest(ExecutorService executorService, Long articleId, String lockType) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3000);
        log.info("lockType: {}", lockType);

        like(articleId, 1L, lockType);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 3000; i++) {
            long userId = i + 2;
            executorService.submit(() -> {
                like(articleId, userId, lockType);
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();
        long endTime = System.currentTimeMillis();

        log.info("lockType: {}, time: {} ms", lockType, endTime - startTime);

        Long count = restClient.get()
                .uri("/v1/article-like/articles/{articleId}/count", articleId)
                .retrieve()
                .body(Long.class);

        log.info("count: {}", count);
    }
}
