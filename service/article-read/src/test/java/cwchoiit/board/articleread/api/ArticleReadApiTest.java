package cwchoiit.board.articleread.api;

import cwchoiit.board.articleread.service.response.ArticleReadPageResponse;
import cwchoiit.board.articleread.service.response.ArticleReadResponse;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class ArticleReadApiTest {
    RestClient articleReadRestClient = RestClient.create("http://localhost:9005");
    RestClient articleRestClient = RestClient.create("http://localhost:9000");

    @Test
    void readTest() {
        ArticleReadResponse response = articleReadRestClient.get()
                .uri("/v1/articles/{articleId}", 138479366407786505L)
                .retrieve()
                .body(ArticleReadResponse.class);

        log.info("response: {}", response);
    }

    @Test
    void readAllTest() {
        ArticleReadPageResponse cacheData = articleReadRestClient.get()
                .uri("/v1/articles?boardId={boardId}&page={page}&pageSize={pageSize}", 1L, 3000L, 5)
                .retrieve()
                .body(ArticleReadPageResponse.class);

        assertThat(cacheData).isNotNull();
        assertThat(cacheData.getArticles().size()).isEqualTo(5);

        ArticleReadPageResponse originData = articleRestClient.get()
                .uri("/v1/articles?boardId={boardId}&page={page}&pageSize={pageSize}", 1L, 3000L, 5)
                .retrieve()
                .body(ArticleReadPageResponse.class);

        assertThat(originData).isNotNull();
        assertThat(originData.getArticles().size()).isEqualTo(5);

        assertThat(cacheData.getArticles().stream().map(ArticleReadResponse::getArticleId).toList())
                .containsExactlyElementsOf(originData.getArticles().stream().map(ArticleReadResponse::getArticleId).toList());
    }

    @Test
    void readAllInfiniteTest() {
        List<ArticleReadResponse> cacheData = articleReadRestClient.get()
                .uri("/v1/articles/infinite?boardId={boardId}&pageSize={pageSize}", 1L, 5L)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assertThat(cacheData).isNotNull();
        assertThat(cacheData.size()).isEqualTo(5);

        List<ArticleReadResponse> originData = articleRestClient.get()
                .uri("/v1/articles/infinite?boardId={boardId}&pageSize={pageSize}", 1L, 5L)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assertThat(originData).isNotNull();
        assertThat(originData.size()).isEqualTo(5);

        assertThat(cacheData.stream().map(ArticleReadResponse::getArticleId).toList())
                .containsExactlyElementsOf(originData.stream().map(ArticleReadResponse::getArticleId).toList());

        // 스크롤 이동

        Long lastArticleIdByCache = cacheData.getLast().getArticleId();
        Long lastArticleIdByOrigin = originData.getLast().getArticleId();

        List<ArticleReadResponse> cacheDataWithLastArticleId = articleReadRestClient.get()
                .uri("/v1/articles/infinite?boardId={boardId}&pageSize={pageSize}&lastArticleId={lastArticleId}", 1L, 5L, lastArticleIdByCache)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assertThat(cacheDataWithLastArticleId).isNotNull();
        assertThat(cacheDataWithLastArticleId.size()).isEqualTo(5);

        List<ArticleReadResponse> originDataWithLastArticleId = articleRestClient.get()
                .uri("/v1/articles/infinite?boardId={boardId}&pageSize={pageSize}&lastArticleId={lastArticleId}", 1L, 5L, lastArticleIdByOrigin)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assertThat(originDataWithLastArticleId).isNotNull();
        assertThat(originDataWithLastArticleId.size()).isEqualTo(5);

        assertThat(cacheDataWithLastArticleId.stream().map(ArticleReadResponse::getArticleId).toList())
                .containsExactlyElementsOf(originDataWithLastArticleId.stream().map(ArticleReadResponse::getArticleId).toList());
    }
}
