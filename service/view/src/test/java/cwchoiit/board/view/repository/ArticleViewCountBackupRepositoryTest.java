package cwchoiit.board.view.repository;

import cwchoiit.board.view.entity.ArticleViewCount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ArticleViewCountBackupRepositoryTest {
    @Autowired
    ArticleViewCountBackupRepository articleViewCountBackupRepository;

    @Test
    @Transactional
    void updateViewCountTest() {
        articleViewCountBackupRepository.save(ArticleViewCount.init(1L, 0L));

        int result1 = articleViewCountBackupRepository.updateViewCount(1L, 100L);
        int result2 = articleViewCountBackupRepository.updateViewCount(1L, 300L);
        int result3 = articleViewCountBackupRepository.updateViewCount(1L, 200L);

        assertThat(result1).isEqualTo(1);
        assertThat(result2).isEqualTo(1);
        assertThat(result3).isEqualTo(0);

        ArticleViewCount articleViewCount = articleViewCountBackupRepository.findById(1L).orElseThrow();
        assertThat(articleViewCount.getViewCount()).isEqualTo(300L);
    }
}