package cwchoiit.board.articleread.service;

import cwchoiit.board.articleread.client.ArticleClient;
import cwchoiit.board.articleread.client.CommentClient;
import cwchoiit.board.articleread.client.LikeClient;
import cwchoiit.board.articleread.client.ViewClient;
import cwchoiit.board.articleread.repository.ArticleQueryModel;
import cwchoiit.board.articleread.repository.ArticleQueryModelRepository;
import cwchoiit.board.articleread.service.event.EventHandler;
import cwchoiit.board.articleread.service.response.ArticleReadResponse;
import cwchoiit.board.common.event.Event;
import cwchoiit.board.common.event.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
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
}
