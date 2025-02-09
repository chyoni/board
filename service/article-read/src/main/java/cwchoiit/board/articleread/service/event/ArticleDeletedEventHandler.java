package cwchoiit.board.articleread.service.event;

import cwchoiit.board.articleread.repository.ArticleQueryModelRepository;
import cwchoiit.board.common.event.Event;
import cwchoiit.board.common.event.payload.ArticleDeletedEventPayload;
import cwchoiit.board.common.event.payload.ArticleUpdatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static cwchoiit.board.common.event.EventType.ARTICLE_DELETED;
import static cwchoiit.board.common.event.EventType.ARTICLE_UPDATED;

@Component
@RequiredArgsConstructor
public class ArticleDeletedEventHandler implements EventHandler<ArticleDeletedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleDeletedEventPayload> event) {
        ArticleDeletedEventPayload payload = event.getPayload();
        articleQueryModelRepository.delete(payload.getArticleId());
    }

    @Override
    public boolean supports(Event<ArticleDeletedEventPayload> event) {
        return ARTICLE_DELETED == event.getType();
    }
}
