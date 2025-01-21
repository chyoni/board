package cwchoiit.board.article.service;

import cwchoiit.board.article.entity.Article;
import cwchoiit.board.article.repository.ArticleRepository;
import cwchoiit.board.article.service.request.ArticleCreateRequest;
import cwchoiit.board.article.service.request.ArticleUpdateRequest;
import cwchoiit.board.article.service.response.ArticlePageResponse;
import cwchoiit.board.article.service.response.ArticleResponse;
import cwchoiit.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final Snowflake snowflake = new Snowflake();
    private final ArticleRepository articleRepository;

    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {
        Article createdArticle = articleRepository.save(
                Article.create(
                        snowflake.nextId(),
                        request.getTitle(),
                        request.getContent(),
                        request.getBoardId(),
                        request.getWriterId()));
        return ArticleResponse.from(createdArticle);
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());
        return ArticleResponse.from(article);
    }

    public ArticleResponse read(Long articleId) {
        return ArticleResponse.from(articleRepository.findById(articleId).orElseThrow());
    }

    @Transactional
    public void delete(Long articleId) {
        articleRepository.deleteById(articleId);
    }

    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        List<ArticleResponse> articles =
                articleRepository.findAll(boardId, (page - 1) * pageSize, pageSize).stream()
                        .map(ArticleResponse::from)
                        .toList();

        Long articleCount = articleRepository.count(
                boardId,
                PageLimitCalculator.calculatePageLimit(page, pageSize, 10L)
        );

        return ArticlePageResponse.of(articles, articleCount);
    }
}
