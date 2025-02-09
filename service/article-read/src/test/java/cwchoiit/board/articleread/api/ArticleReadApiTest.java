package cwchoiit.board.articleread.api;

import cwchoiit.board.articleread.service.response.ArticleReadResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@Slf4j
public class ArticleReadApiTest {
    RestClient restClient = RestClient.create("http://localhost:9005");

    @Test
    void readTest() {
        ArticleReadResponse response = restClient.get()
                .uri("/v1/articles/{articleId}", 138479366407786505L)
                .retrieve()
                .body(ArticleReadResponse.class);

        log.info("response: {}", response);
    }
}
