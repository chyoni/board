package cwchoiit.board.comment.repository;

import cwchoiit.board.comment.entity.ArticleCommentCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleCommentCountRepository extends JpaRepository<ArticleCommentCount, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "update article_comment_count " +
                    "set comment_count = comment_count + 1 " +
                    "where article_id = :articleId",
            nativeQuery = true
    )
    int increase(@Param("articleId") Long articleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "update article_comment_count " +
                    "set comment_count = comment_count - 1 " +
                    "where article_id = :articleId",
            nativeQuery = true
    )
    int decrease(@Param("articleId") Long articleId);
}
