package cwchoiit.board.articleread.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentClient {
    private RestClient restClient;

    @Value("${endpoints.board-comment-service.url}")
    private String commentServiceUrl;

    @PostConstruct
    public void init() {
        restClient = RestClient.create(commentServiceUrl);
    }

    public Long count(Long articleId) {
        try {
            return restClient.get()
                    .uri("/v2/comments/articles/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e) {
            log.error("[count] articleId = {}", articleId, e);
            return 0L;
        }
    }
}
