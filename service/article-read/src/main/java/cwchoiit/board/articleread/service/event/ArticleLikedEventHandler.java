package cwchoiit.board.articleread.service.event;

import cwchoiit.board.articleread.repository.ArticleQueryModelRepository;
import cwchoiit.board.common.event.Event;
import cwchoiit.board.common.event.payload.ArticleLikedEventPayload;
import cwchoiit.board.common.event.payload.CommentCreatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static cwchoiit.board.common.event.EventType.ARTICLE_LIKED;
import static cwchoiit.board.common.event.EventType.COMMENT_CREATED;

@Component
@RequiredArgsConstructor
public class ArticleLikedEventHandler implements EventHandler<ArticleLikedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleLikedEventPayload> event) {
        articleQueryModelRepository.read(event.getPayload().getArticleId())
                .ifPresent(articleQueryModel -> {
                    articleQueryModel.updateBy(event.getPayload());
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<ArticleLikedEventPayload> event) {
        return ARTICLE_LIKED == event.getType();
    }
}
