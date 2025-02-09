package cwchoiit.board.articleread.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.Objects;
import java.util.random.RandomGenerator;

@Slf4j
public class DataInitializer {
    RestClient articleServiceClient = RestClient.create("http://localhost:9000");
    RestClient commentServiceClient = RestClient.create("http://localhost:9001");
    RestClient likeServiceClient = RestClient.create("http://localhost:9002");
    RestClient viewServiceClient = RestClient.create("http://localhost:9003");

    @Test
    void initialize() {
        for (int i = 0; i < 30; i++) {
            Long articleId = createArticle();
            log.info("articleID = {}", articleId);
            long commentCount = RandomGenerator.getDefault().nextLong(10);
            long likeCount = RandomGenerator.getDefault().nextLong(10);
            long viewCount = RandomGenerator.getDefault().nextLong(200);

            createComment(articleId, commentCount);
            like(articleId, likeCount);
            view(articleId, viewCount);
        }
    }

    private void view(Long articleId, Long viewCount) {
        while (viewCount-- > 0) {
            viewServiceClient.post()
                    .uri("/v1/article-view/articles/{articleId}/users/{userId}", articleId, viewCount)
                    .retrieve()
                    .toBodilessEntity();
        }
    }

    private void like(Long articleId, Long likeCount) {
        while (likeCount-- > 0) {
            likeServiceClient.post()
                    .uri("/v1/article-like/articles/{articleId}/users/{userId}/pessimistic-lock-1", articleId, likeCount)
                    .retrieve()
                    .toBodilessEntity();
        }
    }

    private void createComment(Long articleId, Long commentCount) {
        while (commentCount-- > 0) {
            commentServiceClient.post()
                    .uri("/v2/comments")
                    .body(new CommentCreateRequest(articleId, "content", 1L))
                    .retrieve()
                    .toBodilessEntity();
        }
    }

    private Long createArticle() {
        return Objects.requireNonNull(articleServiceClient.post()
                        .uri("/v1/articles")
                        .body(new ArticleCreateRequest("title", "content", 1L, 1L))
                        .retrieve()
                        .body(ArticleResponse.class))
                .getArticleId();
    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    static class ArticleResponse {
        private Long articleId;
    }

    @Getter
    @AllArgsConstructor
    static class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long writerId;
    }
}
