package cwchoiit.board.article.service;

import cwchoiit.board.article.entity.Article;
import cwchoiit.board.article.entity.BoardArticleCount;
import cwchoiit.board.article.repository.ArticleRepository;
import cwchoiit.board.article.repository.BoardArticleCountRepository;
import cwchoiit.board.article.service.request.ArticleCreateRequest;
import cwchoiit.board.article.service.request.ArticleUpdateRequest;
import cwchoiit.board.article.service.response.ArticlePageResponse;
import cwchoiit.board.article.service.response.ArticleResponse;
import cwchoiit.board.common.event.EventType;
import cwchoiit.board.common.event.payload.ArticleCreatedEventPayload;
import cwchoiit.board.common.event.payload.ArticleDeletedEventPayload;
import cwchoiit.board.common.event.payload.ArticleUpdatedEventPayload;
import cwchoiit.board.common.outboxmessagerelay.OutboxEventPublisher;
import cwchoiit.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cwchoiit.board.common.event.EventType.*;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final Snowflake snowflake = new Snowflake();
    private final ArticleRepository articleRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {
        Article createdArticle = articleRepository.save(
                Article.create(
                        snowflake.nextId(),
                        request.getTitle(),
                        request.getContent(),
                        request.getBoardId(),
                        request.getWriterId()));

        int affectedRecord = boardArticleCountRepository.increase(request.getBoardId());
        if (affectedRecord == 0) {
            boardArticleCountRepository.save(BoardArticleCount.init(request.getBoardId(), 1L));
        }

        // 게시글 생성 이벤트를 발생
        outboxEventPublisher.publish(
                ARTICLE_CREATED,
                ArticleCreatedEventPayload.builder()
                        .articleId(createdArticle.getArticleId())
                        .title(createdArticle.getTitle())
                        .content(createdArticle.getContent())
                        .boardId(createdArticle.getBoardId())
                        .writerId(createdArticle.getWriterId())
                        .createdAt(createdArticle.getCreatedAt())
                        .modifiedAt(createdArticle.getModifiedAt())
                        .boardArticleCount(count(createdArticle.getBoardId()))
                        .build(),
                createdArticle.getBoardId()
        );
        return ArticleResponse.from(createdArticle);
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());
        // 게시글 수정 이벤트 발행
        outboxEventPublisher.publish(
                ARTICLE_UPDATED,
                ArticleUpdatedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .build(),
                article.getBoardId()
        );
        return ArticleResponse.from(article);
    }

    public ArticleResponse read(Long articleId) {
        return ArticleResponse.from(articleRepository.findById(articleId).orElseThrow());
    }

    @Transactional
    public void delete(Long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        articleRepository.delete(article);
        boardArticleCountRepository.decrease(article.getBoardId());
        // 게시글 삭제 이벤트 발행
        outboxEventPublisher.publish(
                ARTICLE_DELETED,
                ArticleDeletedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .build(),
                article.getBoardId()
        );
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

    public List<ArticleResponse> readAllInfinite(Long boardId, Long pageSize, Long lastArticleId) {
        List<Article> articles = lastArticleId == null ?
                articleRepository.findAllInfinite(boardId, pageSize) :
                articleRepository.findAllInfinite(boardId, pageSize, lastArticleId);

        return articles.stream()
                .map(ArticleResponse::from)
                .toList();
    }

    public Long count(Long boardId) {
        return boardArticleCountRepository.findById(boardId)
                .map(BoardArticleCount::getArticleCount)
                .orElse(0L);
    }
}
