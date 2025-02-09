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
public class LikeClient {
    private RestClient restClient;

    @Value("${endpoints.board-like-service.url}")
    private String likeServiceUrl;

    @PostConstruct
    public void init() {
        restClient = RestClient.create(likeServiceUrl);
    }

    public Long count(Long articleId) {
        try {
            return restClient.get()
                    .uri("/v1/article-like/articles/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e) {
            log.error("[count] articleId = {}", articleId, e);
            return 0L;
        }
    }
}
