package cwchoiit.board.comment.repository;

import cwchoiit.board.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 최대 2 Depth 설계인 경우, 자식 커멘트가 있는지 확인하는 방법은 아래 쿼리에서
     * articleId, parentCommentId 를 지정하고 limit = 2 로 설정했을 때 결과 개수가 2라면 자식 커멘트가 있는 것이다.
     * 왜냐하면, 부모가 없는 커멘트는 본인 아이디가 곧 parentCommentId, 부모가 있는 커멘트는 parentCommentId 가 특정 부모 커멘트의 ID 이므로
     * ID가 1인 커멘트가 있고 ID가 2인 커멘트가 있을 때 ID가 2인 커멘트의 부모가 1일때, parentCommentId가 1인 결과 개수가 2로 계산이 가능하다.
     * 즉, articleId = 1, parentCommentId = 1, limit = 2 조건으로 아래 쿼리 결과가 2라면 자식이 있다라고 판단할 수 있다.
     *
     * @param articleId       게시글 ID
     * @param parentCommentId 부모 커멘트 ID
     * @param limit           limit
     * @return 쿼리의 결과 개수
     */
    @Query(
            value = "select count(*) from (" +
                    "   select comment_id " +
                    "   from comment " +
                    "   where article_id = :articleId and parent_comment_id = :parentCommentId " +
                    "   limit :limit" +
                    ") t",
            nativeQuery = true
    )
    Long countBy(@Param("articleId") Long articleId,
                 @Param("parentCommentId") Long parentCommentId,
                 @Param("limit") Long limit);

    // #########################
    // 페이지 번호 방식
    // #########################
    @Query(
            value = "select comment.comment_id, comment.content, comment.parent_comment_id, comment.article_id, comment.writer_id, comment.deleted, comment.created_at " +
                    "from (" +
                    "   select comment_id " +
                    "   from comment " +
                    "   where article_id = :articleId " +
                    "   order by parent_comment_id, comment_id " +
                    "   limit :limit offset :offset " +
                    ") t left join comment on t.comment_id = comment.comment_id",
            nativeQuery = true
    )
    List<Comment> findAll(@Param("articleId") Long articleId,
                          @Param("offset") Long offset,
                          @Param("limit") Long limit);

    @Query(
            value = "select count(*) from (" +
                    "   select comment_id " +
                    "   from comment " +
                    "   where article_id = :articleId " +
                    "   limit :limit " +
                    ") t",
            nativeQuery = true
    )
    Long count(@Param("articleId") Long articleId, @Param("limit") Long limit);

    // #########################
    // 무한 스크롤 방식
    // #########################
    @Query(
            value = "select comment.comment_id, comment.content, comment.parent_comment_id, comment.article_id, comment.writer_id, comment.deleted, comment.created_at " +
                    "from comment " +
                    "where article_id = :articleId " +
                    "order by parent_comment_id, comment_id " +
                    "limit :limit",
            nativeQuery = true
    )
    List<Comment> findAllInfinite(@Param("articleId") Long articleId, @Param("limit") Long limit);

    @Query(
            value = "select comment.comment_id, comment.content, comment.parent_comment_id, comment.article_id, comment.writer_id, comment.deleted, comment.created_at " +
                    "from comment " +
                    "where article_id = :articleId " +
                    "and (parent_comment_id, comment_id) > (:lastParentCommentId, :lastCommentId) " +
                    "order by parent_comment_id, comment_id " +
                    "limit :limit",
            nativeQuery = true
    )
    List<Comment> findAllInfinite(@Param("articleId") Long articleId,
                                  @Param("limit") Long limit,
                                  @Param("lastParentCommentId") Long lastParentCommentId,
                                  @Param("lastCommentId") Long lastCommentId);
}
