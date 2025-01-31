package cwchoiit.board.view.repository;

import cwchoiit.board.view.entity.ArticleViewCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleViewCountBackupRepository extends JpaRepository<ArticleViewCount, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "update article_view_count " +
                    "set view_count = :viewCount " +
                    "where article_id = :articleId and view_count < :viewCount",
            nativeQuery = true
    )
    int updateViewCount(@Param("articleId") Long articleId, @Param("viewCount") Long viewCount);
}
