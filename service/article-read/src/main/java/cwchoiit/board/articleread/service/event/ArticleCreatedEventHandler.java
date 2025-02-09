package cwchoiit.board.articleread.service.event;

import cwchoiit.board.articleread.repository.ArticleQueryModel;
import cwchoiit.board.articleread.repository.ArticleQueryModelRepository;
import cwchoiit.board.common.event.Event;
import cwchoiit.board.common.event.payload.ArticleCreatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static cwchoiit.board.common.event.EventType.ARTICLE_CREATED;

@Component
@RequiredArgsConstructor
public class ArticleCreatedEventHandler implements EventHandler<ArticleCreatedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();
        articleQueryModelRepository.create(
                ArticleQueryModel.create(payload),
                Duration.ofDays(1)
        );
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> event) {
        return ARTICLE_CREATED == event.getType();
    }
}
