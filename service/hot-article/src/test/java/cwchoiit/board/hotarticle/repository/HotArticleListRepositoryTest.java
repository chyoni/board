package cwchoiit.board.hotarticle.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HotArticleListRepositoryTest {
    @Autowired
    HotArticleListRepository hotArticleListRepository;

    @Test
    void addTest() throws InterruptedException {
        LocalDateTime time = LocalDateTime.of(2024, 7, 23, 0, 0);
        long limit = 3L;

        hotArticleListRepository.add(1L, time, 2L, limit, Duration.ofSeconds(3));
        hotArticleListRepository.add(2L, time, 3L, limit, Duration.ofSeconds(3));
        hotArticleListRepository.add(3L, time, 1L, limit, Duration.ofSeconds(3));
        hotArticleListRepository.add(4L, time, 5L, limit, Duration.ofSeconds(3));
        hotArticleListRepository.add(5L, time, 4L, limit, Duration.ofSeconds(3));

        List<Long> articleIds = hotArticleListRepository.readAll("20240723");

        assertThat(articleIds).hasSize(3);
        assertThat(articleIds).containsExactly(4L, 5L, 2L); // score 가 높은 순으로 3개

        Thread.sleep(5000);

        assertThat(hotArticleListRepository.readAll("20240723")).isEmpty(); // TTL 을 3초로 설정했으니 3초가 지나면 다 없어져야 함
    }
}