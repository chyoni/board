package cwchoiit.board.article;

import cwchoiit.board.article.service.request.ArticleCreateRequest;
import cwchoiit.board.article.service.request.ArticleUpdateRequest;
import cwchoiit.board.article.service.response.ArticlePageResponse;
import cwchoiit.board.article.service.response.ArticleResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArticleApiTest {

    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        ArticleResponse response = create(new ArticleCreateRequest("hi", "my content", 1L, 1L));
        assertThat(response.getTitle()).isEqualTo("hi");
        assertThat(response.getContent()).isEqualTo("my content");
    }

    @Test
    void readTest() {
        ArticleResponse response = read(138465139304538112L);
        assertThat(response).isNotNull();
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
                .uri("/v1/articles?boardId=1&page=1&pageSize=30")
                .retrieve()
                .body(ArticlePageResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getArticles()).isNotEmpty();
        assertThat(response.getArticleCount()).isEqualTo(301L);

        ArticlePageResponse response2 = restClient.get()
                .uri("/v1/articles?boardId=1&page=50000&pageSize=30")
                .retrieve()
                .body(ArticlePageResponse.class);

        assertThat(response2).isNotNull();
        assertThat(response2.getArticles()).isNotEmpty();
    }

    @Test
    void readAllInfiniteTest() {
        List<ArticleResponse> response = restClient.get()
                .uri("/v1/articles/infinite?boardId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(5L);

        Long lastArticleId = response.getLast().getArticleId();
        List<ArticleResponse> nextResponse = restClient.get()
                .uri("/v1/articles/infinite?boardId=1&pageSize=5&lastArticleId={lastArticleId}", lastArticleId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        assertThat(nextResponse).isNotNull();
        assertThat(nextResponse.size()).isEqualTo(5L);
    }

    @Test
    void updateTest() {
        ArticleResponse updated = update(138465139304538112L);
        assertThat(updated).isNotNull();
        assertThat(updated.getTitle()).isEqualTo("hello");
    }

    @Test
    void deleteTest() {
        restClient.delete()
                .uri("/v1/articles/{articleId}", 138465139304538112L)
                .retrieve().toBodilessEntity();

        assertThatThrownBy(() -> read(138465139304538112L)).isInstanceOf(HttpServerErrorException.class);
    }

    @Test
    void countTest() {
        ArticleResponse response = create(new ArticleCreateRequest("hi", "my content", 1L, 2L));

        Long count = restClient.get()
                .uri("/v1/articles/boards/{boardId}/count", 2L)
                .retrieve()
                .body(Long.class);

        assertThat(count).isEqualTo(1L);

        restClient.delete()
                .uri("/v1/articles/{articleId}", response.getArticleId())
                .retrieve()
                .toBodilessEntity();

        Long reCount = restClient.get()
                .uri("/v1/articles/boards/{boardId}/count", 2L)
                .retrieve()
                .body(Long.class);

        assertThat(reCount).isEqualTo(0L);
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
                .uri("/v1/articles")
                .body(request)
                .retrieve()
                .body(ArticleResponse.class);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    ArticleResponse update(Long articleId) {
        return restClient.put()
                .uri("/v1/articles/{articleId}", articleId)
                .body(new ArticleUpdateRequest("hello", "world"))
                .retrieve()
                .body(ArticleResponse.class);
    }
}