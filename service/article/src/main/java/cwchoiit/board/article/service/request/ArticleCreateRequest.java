package cwchoiit.board.article.service.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class ArticleCreateRequest {
    private String title;
    private String content;
    private Long writerId;
    private Long boardId;
}
