package cwchoiit.board.comment.api;

import cwchoiit.board.comment.service.request.CommentCreateRequestV2;
import cwchoiit.board.comment.service.response.CommentPageResponseV2;
import cwchoiit.board.comment.service.response.CommentResponseV2;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
public class CommentApiV2Test {

    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponseV2 response1 = create(new CommentCreateRequestV2(1L, "my comment1", null, 1L));
        CommentResponseV2 response2 = create(new CommentCreateRequestV2(1L, "my comment2", response1.getCommentPath(), 1L));
        CommentResponseV2 response3 = create(new CommentCreateRequestV2(1L, "my comment3", response2.getCommentPath(), 1L));

        log.debug("response1.getCommentPath()(): {}", response1.getCommentPath());
        log.debug("response2.getCommentPath()(): {}", response2.getCommentPath());
        log.debug("response3.getCommentPath()(): {}", response3.getCommentPath());
    }

    CommentResponseV2 create(CommentCreateRequestV2 request) {
        return restClient.post()
                .uri("/v2/comments")
                .body(request)
                .retrieve()
                .body(CommentResponseV2.class);
    }

    @Test
    void readAll() {
        CommentPageResponseV2 response = restClient.get()
                .uri("/v2/comments?articleId=1&page=50000&pageSize=10")
                .retrieve()
                .body(CommentPageResponseV2.class);

        assertThat(response).isNotNull();
        assertThat(response.getComments()).hasSize(10);
    }

    @Test
    void readAllInfinite() {
        List<CommentResponseV2> response = restClient.get()
                .uri("/v2/comments/infinite?articleId=1&limit=5")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assertThat(response).isNotNull();
        assertThat(response).hasSize(5);

        String lastPath = response.getLast().getCommentPath();
        List<CommentResponseV2> response2 = restClient.get()
                .uri("/v2/comments/infinite?articleId=1&limit=5&lastPath={lastPath}", lastPath)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assertThat(response2).isNotNull();
        assertThat(response2).hasSize(5);
    }
}
