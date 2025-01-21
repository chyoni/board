package cwchoiit.board.article.repository;

import cwchoiit.board.article.entity.Article;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class ArticleRepositoryTest {

    @Autowired
    ArticleRepository articleRepository;

    @Test
    void findAllTest() {
        List<Article> articles = articleRepository.findAll(1L, 1499970L, 30L);
        assertThat(articles).hasSize(30);
    }

    @Test
    void countTest() {
        Long count = articleRepository.count(1L, 10000L);
        assertThat(count).isEqualTo(10000L);
    }
}