package cwchoiit.board.hotarticle.client;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

/**
 * 인기글 서비스에서는, 인기글을 조회하기 위해 게시글 서비스에 요청을 해야 한다.
 * 그 요청을 처리하는 클래스.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleClient {
    private RestClient restClient;

    @Value("${endpoints.board-article-service.url}")
    private String articleServiceUrl;

    @PostConstruct
    void init() {
        restClient = RestClient.create(articleServiceUrl);
    }

    public ArticleResponse read(Long articleId) {
        try {
            return restClient.get()
                    .uri("/v1/articles/{articleId}", articleId)
                    .retrieve()
                    .body(ArticleResponse.class);
        } catch (Exception e) {
            log.error("[read] articleId = {}", articleId, e);
            return null;
        }
    }

    @Getter
    public static class ArticleResponse {
        private Long articleId;
        private String title;
        private LocalDateTime createdAt;
    }
}
