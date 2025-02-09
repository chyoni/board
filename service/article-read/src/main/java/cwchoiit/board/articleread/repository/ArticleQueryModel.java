package cwchoiit.board.articleread.repository;

import cwchoiit.board.articleread.client.ArticleClient;
import cwchoiit.board.common.event.payload.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ArticleQueryModel {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long articleCommentCount;
    private Long articleLikeCount;

    public static ArticleQueryModel create(ArticleCreatedEventPayload payload) {
        ArticleQueryModel articleQueryModel = mappingArticleQueryModel(
                payload.getArticleId(),
                payload.getTitle(),
                payload.getContent(),
                payload.getBoardId(),
                payload.getWriterId(),
                payload.getCreatedAt(),
                payload.getModifiedAt()
        );
        articleQueryModel.articleCommentCount = 0L;
        articleQueryModel.articleLikeCount = 0L;
        return articleQueryModel;
    }

    public static ArticleQueryModel create(ArticleClient.ArticleResponse article,
                                           Long articleCommentCount,
                                           Long articleLikeCount) {
        ArticleQueryModel articleQueryModel = mappingArticleQueryModel(
                article.getArticleId(),
                article.getTitle(),
                article.getContent(),
                article.getBoardId(),
                article.getWriterId(),
                article.getCreatedAt(),
                article.getModifiedAt()
        );
        articleQueryModel.articleCommentCount = articleCommentCount;
        articleQueryModel.articleLikeCount = articleLikeCount;
        return articleQueryModel;
    }

    public void updateBy(CommentCreatedEventPayload payload) {
        articleCommentCount = payload.getArticleCommentCount();
    }

    public void updateBy(CommentDeletedEventPayload payload) {
        articleCommentCount = payload.getArticleCommentCount();
    }

    public void updateBy(ArticleLikedEventPayload payload) {
        articleCommentCount = payload.getArticleLikeCount();
    }

    public void updateBy(ArticleDislikedEventPayload payload) {
        articleCommentCount = payload.getArticleLikeCount();
    }

    public void updateBy(ArticleUpdatedEventPayload payload) {
        title = payload.getTitle();
        content = payload.getContent();
        boardId = payload.getBoardId();
        writerId = payload.getWriterId();
        createdAt = payload.getCreatedAt();
        modifiedAt = payload.getModifiedAt();
    }

    private static ArticleQueryModel mappingArticleQueryModel(Long articleId,
                                                              String title,
                                                              String content,
                                                              Long boardId,
                                                              Long writerId,
                                                              LocalDateTime createdAt,
                                                              LocalDateTime modifiedAt) {
        ArticleQueryModel articleQueryModel = new ArticleQueryModel();
        articleQueryModel.articleId = articleId;
        articleQueryModel.title = title;
        articleQueryModel.content = content;
        articleQueryModel.boardId = boardId;
        articleQueryModel.writerId = writerId;
        articleQueryModel.createdAt = createdAt;
        articleQueryModel.modifiedAt = modifiedAt;
        return articleQueryModel;
    }
}
