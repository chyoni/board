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

    /**
     * 게시글이 최초 생성된 경우에 대한 이벤트를 받을 때 사용될 팩토리 메서드.
     * @param payload 게시글 생성 데이터
     * @return {@link ArticleQueryModel}
     */
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

    /**
     * 기존에 있는 게시글을 현재 게시글 조회 최적화 서비스에서 찾지 못한 경우, 실제 서비스에서 데이터를 가져온다.
     * 그 가져온 데이터를 게시글 조회 최적화 서비스의 데이터베이스인 {@code Redis}에 저장할 때 사용되는 팩토리 메서드.
     * @param article 게시글 데이터
     * @param articleCommentCount 게시글 댓글 수
     * @param articleLikeCount 게시글 좋아요 수
     * @return {@link ArticleQueryModel}
     */
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
        articleLikeCount = payload.getArticleLikeCount();
    }

    public void updateBy(ArticleDislikedEventPayload payload) {
        articleLikeCount = payload.getArticleLikeCount();
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
