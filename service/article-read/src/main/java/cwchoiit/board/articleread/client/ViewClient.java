package cwchoiit.board.articleread.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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

    /**
     * {@code @Cacheable} 동작 과정은 다음과 같다.
     * Redis 에서 데이터를 조회해본다.
     * [Redis 에 데이터가 없었다면], count 메서드 내부 로직이 호출되면서, viewService 로 원본 데이터를 요청한다. 그리고 Redis 에 데이터를 넣고 응답한다.
     * [만약, Redis 에 데이터가 있었다면], 그 데이터를 그대로 반환한다.
     * @param articleId articleId
     * @return view count
     */
    @Cacheable(key = "#articleId", value = "articleViewCount") // articleViewCount::{articleId} / {조회수값}
    public Long count(Long articleId) {
        log.info("[count] articleId: {}", articleId);
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
