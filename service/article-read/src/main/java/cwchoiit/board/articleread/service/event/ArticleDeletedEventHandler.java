package cwchoiit.board.articleread.service.event;

import cwchoiit.board.articleread.repository.ArticleIdListRepository;
import cwchoiit.board.articleread.repository.ArticleQueryModelRepository;
import cwchoiit.board.articleread.repository.BoardArticleCountRepository;
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
    private final ArticleIdListRepository articleIdListRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    @Override
    public void handle(Event<ArticleDeletedEventPayload> event) {
        ArticleDeletedEventPayload payload = event.getPayload();
        // 사소한 디테일이긴 하지만, 목록에서 먼저 삭제해주는 것은 이유가 있다. 만약, 게시글 자체를 삭제하는 코드가 먼저 있다면, 찰나의 순간에
        // 사용자가 목록에선 아직 지워지지 않았기에 목록에서 지워진 게시글을 클릭할 수가 있는데, 그를 방지하고자 진입점을 먼저 삭제하는 것
        articleIdListRepository.delete(payload.getBoardId(), payload.getArticleId());

        articleQueryModelRepository.delete(payload.getArticleId());
        boardArticleCountRepository.createOrUpdate(payload.getBoardId(), payload.getBoardArticleCount());
    }

    @Override
    public boolean supports(Event<ArticleDeletedEventPayload> event) {
        return ARTICLE_DELETED == event.getType();
    }
}
