package cwchoiit.board.hotarticle.service.eventhandler;

import cwchoiit.board.common.event.Event;
import cwchoiit.board.common.event.payload.ArticleDislikedEventPayload;
import cwchoiit.board.hotarticle.repository.ArticleLikeCountRepository;
import cwchoiit.board.hotarticle.utils.TimeCalculatorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static cwchoiit.board.common.event.EventType.ARTICLE_DISLIKED;

@Component
@RequiredArgsConstructor
public class ArticleDislikedEventHandler implements EventHandler<ArticleDislikedEventPayload> {
    private final ArticleLikeCountRepository articleLikeCountRepository;

    @Override
    public void handle(Event<ArticleDislikedEventPayload> event) {
        ArticleDislikedEventPayload payload = event.getPayload();
        articleLikeCountRepository.createOrUpdate(
                payload.getArticleId(),
                payload.getArticleLikeCount(),
                TimeCalculatorUtils.calculateDurationToMidnight()
        );
    }

    @Override
    public boolean supports(Event<ArticleDislikedEventPayload> event) {
        return ARTICLE_DISLIKED == event.getType();
    }

    @Override
    public Long findArticleId(Event<ArticleDislikedEventPayload> event) {
        return event.getPayload().getArticleId();
    }
}
