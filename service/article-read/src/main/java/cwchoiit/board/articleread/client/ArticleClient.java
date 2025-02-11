package cwchoiit.board.articleread.client;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleClient {
    private RestClient restClient;

    @Value("${endpoints.board-article-service.url}")
    private String articleServiceUrl;

    @PostConstruct
    public void init() {
        restClient = RestClient.create(articleServiceUrl);
    }

    public Optional<ArticleResponse> read(Long articleId) {
        try {
            ArticleResponse response = restClient.get()
                    .uri("/v1/articles/{articleId}", articleId)
                    .retrieve()
                    .body(ArticleResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("[read] articleId = {}", articleId, e);
            return Optional.empty();
        }
    }

    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        try {
            return restClient.get()
                    .uri("/v1/articles?boardId={boardId}&page={page}&pageSize={pageSize}", boardId, page, pageSize)
                    .retrieve()
                    .body(ArticlePageResponse.class);
        } catch (Exception e) {
            log.error("[readAll] boardId = {}, page = {}, pageSize = {}", boardId, page, pageSize, e);
            return ArticlePageResponse.EMPTY;
        }
    }

    public List<ArticleResponse> readAllInfinite(Long boardId, Long lastArticleId, Long pageSize) {
        try {
            return restClient.get()
                    .uri(lastArticleId != null ?
                            "/v1/articles/infinite?boardId=%s&lastArticleId=%s&pageSize=%s".formatted(boardId, lastArticleId, pageSize) :
                            "/v1/articles/infinite?boardId=%s&pageSize=%s".formatted(boardId, pageSize)
                    ).retrieve().body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("[readAllInfinite] boardId = {}, lastArticleId = {}, pageSize = {}", boardId, lastArticleId, pageSize, e);
            return List.of();
        }
    }

    public Long count(Long boardId) {
        try {
            return restClient.get()
                    .uri("/v1/articles/boards/{boardId}/count", boardId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e) {
            log.error("[count] boardId = {}", boardId, e);
            return 0L;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticlePageResponse {
        private List<ArticleResponse> articles;
        private Long articleCount;

        public static ArticlePageResponse EMPTY = new ArticlePageResponse(List.of(), 0L);
    }

    @Getter
    public static class ArticleResponse {
        private Long articleId;
        private String title;
        private String content;
        private Long boardId;
        private Long writerId;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
