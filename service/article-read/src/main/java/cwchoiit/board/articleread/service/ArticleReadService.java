package cwchoiit.board.articleread.service;

import cwchoiit.board.articleread.client.ArticleClient;
import cwchoiit.board.articleread.client.CommentClient;
import cwchoiit.board.articleread.client.LikeClient;
import cwchoiit.board.articleread.client.ViewClient;
import cwchoiit.board.articleread.repository.ArticleIdListRepository;
import cwchoiit.board.articleread.repository.ArticleQueryModel;
import cwchoiit.board.articleread.repository.ArticleQueryModelRepository;
import cwchoiit.board.articleread.repository.BoardArticleCountRepository;
import cwchoiit.board.articleread.service.event.EventHandler;
import cwchoiit.board.articleread.service.response.ArticleReadPageResponse;
import cwchoiit.board.articleread.service.response.ArticleReadResponse;
import cwchoiit.board.common.event.Event;
import cwchoiit.board.common.event.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleReadService {
    private final ArticleClient articleClient;
    private final CommentClient commentClient;
    private final LikeClient likeClient;
    private final ViewClient viewClient;
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final ArticleIdListRepository articleIdListRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;
    private final List<EventHandler> eventHandlers;

    public void handleEvent(Event<EventPayload> event) {
        for (EventHandler eventHandler : eventHandlers) {
            if (eventHandler.supports(event)) {
                eventHandler.handle(event);
            }
        }
    }

    public ArticleReadResponse read(Long articleId) {
        ArticleQueryModel articleQueryModel = articleQueryModelRepository.read(articleId)
                .or(() -> fetch(articleId))
                .orElseThrow();

        return ArticleReadResponse.from(articleQueryModel, viewClient.count(articleId));
    }

    public ArticleReadPageResponse readAll(Long boardId, Long page, Long pageSize) {
        List<Long> articleIds = readAllArticleIds(boardId, page, pageSize);
        return ArticleReadPageResponse.of(readAll(articleIds), count(boardId));
    }

    public List<ArticleReadResponse> readAllInfinite(Long boardId, Long lastArticleId, Long pageSize) {
        return readAll(readAllInfiniteArticleIds(boardId, lastArticleId, pageSize));
    }

    private List<Long> readAllInfiniteArticleIds(Long boardId, Long lastArticleId, Long pageSize) {
        List<Long> articleIds = articleIdListRepository.readAllInfinite(boardId, lastArticleId, pageSize);

        if (pageSize == articleIds.size()) {
            log.info("[readAllInfiniteArticleIds] return redis data.");
            return articleIds;
        }

        log.info("[readAllInfiniteArticleIds] return origin data.");
        return articleClient.readAllInfinite(boardId, lastArticleId, pageSize).stream()
                .map(ArticleClient.ArticleResponse::getArticleId)
                .toList();
    }

    private Optional<ArticleQueryModel> fetch(Long articleId) {
        Optional<ArticleQueryModel> optionalArticleQueryModel = articleClient.read(articleId)
                .map(article -> ArticleQueryModel.create(
                        article,
                        commentClient.count(articleId),
                        likeClient.count(articleId))
                );

        optionalArticleQueryModel
                .ifPresent(articleQueryModel -> articleQueryModelRepository.create(articleQueryModel, Duration.ofDays(1)));

        log.info("[fetch] fetched data. articleId: {}, isPresent: {}", articleId, optionalArticleQueryModel.isPresent());
        return optionalArticleQueryModel;
    }

    private List<ArticleReadResponse> readAll(List<Long> articleIds) {
        Map<Long, ArticleQueryModel> articleQueryModelMap = articleQueryModelRepository.readAll(articleIds);
        return articleIds.stream()
                .map(articleId -> articleQueryModelMap.containsKey(articleId) ?
                        articleQueryModelMap.get(articleId) :
                        fetch(articleId).orElse(null)
                )
                .filter(Objects::nonNull)
                .map(articleQueryModel ->
                        ArticleReadResponse.from(
                                articleQueryModel,
                                viewClient.count(articleQueryModel.getArticleId())
                        )
                )
                .toList();
    }

    private List<Long> readAllArticleIds(Long boardId, Long page, Long pageSize) {
        List<Long> articleIds = articleIdListRepository.readAll(boardId, (page - 1) * pageSize, pageSize);

        if (pageSize == articleIds.size()) {
            log.info("[readAllArticleIds] return redis data.");
            return articleIds;
        }

        log.info("[readAllArticleIds] return origin data.");
        return articleClient.readAll(boardId, page, pageSize).getArticles()
                .stream()
                .map(ArticleClient.ArticleResponse::getArticleId)
                .toList();
    }
    
    private long count(Long boardId) {
        Long boardArticleCount = boardArticleCountRepository.read(boardId);
        if (boardArticleCount != null) {
            return boardArticleCount;
        }
        Long originBoardArticleCount = articleClient.count(boardId);
        boardArticleCountRepository.createOrUpdate(boardId, originBoardArticleCount);
        return originBoardArticleCount;
    }
}
