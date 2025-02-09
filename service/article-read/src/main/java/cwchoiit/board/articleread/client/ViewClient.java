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
public class ViewClient {
    private RestClient restClient;

    @Value("${endpoints.board-view-service.url}")
    private String viewServiceUrl;

    @PostConstruct
    public void init() {
        restClient = RestClient.create(viewServiceUrl);
    }

    public Long count(Long articleId) {
        try {
            return restClient.get()
                    .uri("/v1/article-view/articles/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e) {
            log.error("[count] articleId = {}", articleId, e);
            return 0L;
        }
    }
}
