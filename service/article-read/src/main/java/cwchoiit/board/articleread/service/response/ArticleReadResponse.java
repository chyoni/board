package cwchoiit.board.articleread.service.response;

import cwchoiit.board.articleread.repository.ArticleQueryModel;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class ArticleReadResponse {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long articleCommentCount;
    private Long articleLikeCount;

    private Long articleViewCount;

    public static ArticleReadResponse from(ArticleQueryModel queryModel, Long articleViewCount) {
        ArticleReadResponse articleReadResponse = new ArticleReadResponse();
        articleReadResponse.articleId = queryModel.getArticleId();
        articleReadResponse.title = queryModel.getTitle();
        articleReadResponse.content = queryModel.getContent();
        articleReadResponse.boardId = queryModel.getBoardId();
        articleReadResponse.writerId = queryModel.getWriterId();
        articleReadResponse.createdAt = queryModel.getCreatedAt();
        articleReadResponse.modifiedAt = queryModel.getModifiedAt();
        articleReadResponse.articleCommentCount = queryModel.getArticleCommentCount();
        articleReadResponse.articleLikeCount = queryModel.getArticleLikeCount();
        articleReadResponse.articleViewCount = articleViewCount;
        return articleReadResponse;
    }
}
