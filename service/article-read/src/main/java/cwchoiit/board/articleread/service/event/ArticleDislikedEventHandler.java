package cwchoiit.board.articleread.service.event;

import cwchoiit.board.articleread.repository.ArticleQueryModelRepository;
import cwchoiit.board.common.event.Event;
import cwchoiit.board.common.event.payload.ArticleDislikedEventPayload;
import cwchoiit.board.common.event.payload.ArticleLikedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static cwchoiit.board.common.event.EventType.ARTICLE_DISLIKED;
import static cwchoiit.board.common.event.EventType.ARTICLE_LIKED;

@Component
@RequiredArgsConstructor
public class ArticleDislikedEventHandler implements EventHandler<ArticleDislikedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleDislikedEventPayload> event) {
        articleQueryModelRepository.read(event.getPayload().getArticleId())
                .ifPresent(articleQueryModel -> {
                    articleQueryModel.updateBy(event.getPayload());
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<ArticleDislikedEventPayload> event) {
        return ARTICLE_DISLIKED == event.getType();
    }
}
