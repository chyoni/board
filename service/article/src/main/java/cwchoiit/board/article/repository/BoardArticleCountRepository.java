package cwchoiit.board.article.repository;

import cwchoiit.board.article.entity.BoardArticleCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardArticleCountRepository extends JpaRepository<BoardArticleCount, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "update board_article_count " +
                    "set article_count = article_count + 1 " +
                    "where board_id = :boardId",
            nativeQuery = true
    )
    int increase(@Param("boardId") Long boardId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "update board_article_count " +
                    "set article_count = article_count - 1 " +
                    "where board_id = :boardId",
            nativeQuery = true
    )
    int decrease(@Param("boardId") Long boardId);
}
