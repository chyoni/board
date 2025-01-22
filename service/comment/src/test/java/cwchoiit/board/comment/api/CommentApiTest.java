package cwchoiit.board.comment.api;

import cwchoiit.board.comment.service.request.CommentCreateRequest;
import cwchoiit.board.comment.service.response.CommentResponse;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class CommentApiTest {

    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = createComment(new CommentCreateRequest(1L, "my content1", null, 1L));
        CommentResponse response2 = createComment(new CommentCreateRequest(1L, "my content2", response1.getCommentId(), 1L));
        CommentResponse response3 = createComment(new CommentCreateRequest(1L, "my content3", response1.getCommentId(), 1L));

        assertThat(response1).isNotNull();
        assertThat(response2).isNotNull();
        assertThat(response3).isNotNull();
        assertThat(response2.getParentCommentId()).isEqualTo(response1.getCommentId());
        assertThat(response3.getParentCommentId()).isEqualTo(response1.getCommentId());
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v1/comments/{commentId}", 140429496215494656L)
                .retrieve()
                .body(CommentResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getCommentId()).isEqualTo(140429496215494656L);
    }

    @Test
    void delete() {
        restClient.delete()
                .uri("/v1/comments/{commentId}", 140429496215494656L)
                .retrieve()
                .toBodilessEntity();

        CommentResponse response = restClient.get()
                .uri("/v1/comments/{commentId}", 140429496215494656L)
                .retrieve()
                .body(CommentResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getCommentId()).isEqualTo(140429496215494656L);
        assertThat(response.getDeleted()).isTrue();
    }

    CommentResponse createComment(CommentCreateRequest request) {
        return restClient.post()
                .uri("/v1/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

}
