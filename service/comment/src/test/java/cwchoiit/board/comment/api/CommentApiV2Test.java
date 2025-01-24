package cwchoiit.board.comment.api;

import cwchoiit.board.comment.service.request.CommentCreateRequestV2;
import cwchoiit.board.comment.service.response.CommentResponseV2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

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
}
