package cwchoiit.board.comment.service.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class CommentCreateRequestV2 {
    private Long articleId;
    private String content;
    private String parentPath;
    private Long writerId;
}
