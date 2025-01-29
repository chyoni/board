package cwchoiit.board.like.api;

import cwchoiit.board.like.service.response.ArticleLikeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LikeApiTest {
    RestClient restClient = RestClient.create("http://localhost:9002");

    @Test
    void likeAndUnlikeTest() {
        Long articleId = 9999L;

        like(articleId, 1L);
        like(articleId, 2L);
        like(articleId, 3L);

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

    void like(Long articleId, Long userId) {
        restClient.post()
                .uri("/v1/article-like/articles/{articleId}/users/{userId}", articleId, userId)
                .retrieve().toBodilessEntity();
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
}
