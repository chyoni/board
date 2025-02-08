package cwchoiit.board.hotarticle.api;

import cwchoiit.board.hotarticle.service.response.HotArticleResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
public class HotArticleApiTest {
    RestClient restClient = RestClient.create("http://localhost:9004");

    @Test
    void readAllTest() {
        List<HotArticleResponse> response = restClient.get()
                .uri("/v1/hot-articles/articles/date/{dateStr}", "20250208")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assert response != null;
        for (HotArticleResponse hotArticleResponse : response) {
            log.info("Hot article title: {}", hotArticleResponse.getTitle());
        }
    }
}
