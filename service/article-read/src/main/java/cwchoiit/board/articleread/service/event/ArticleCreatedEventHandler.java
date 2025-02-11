package cwchoiit.board.articleread.service.event;

import cwchoiit.board.articleread.repository.ArticleIdListRepository;
import cwchoiit.board.articleread.repository.ArticleQueryModel;
import cwchoiit.board.articleread.repository.ArticleQueryModelRepository;
import cwchoiit.board.articleread.repository.BoardArticleCountRepository;
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
    private final ArticleIdListRepository articleIdListRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();
        articleQueryModelRepository.create(
                ArticleQueryModel.create(payload),
                Duration.ofDays(1)
        );

        // 사소한 디테일이지만, 목록에 추가하는 것을 나중에 하는것도 이유가 있다. 게시글을 저장하기 전에 목록에 먼저 저장을 하면,
        // 그 찰나의 순간에 사용자가 목록에 보이는 아직 게시글이 저장되지 않은 게시글을 선택할 수가 있는데 그렇게 되면 여기 Redis 에는 저장된 상태가 아니기 때문에
        // 실제 게시글 서비스에 요청을 할 것이다. 상당히 비효울적인 흐름이 발생할 수 있는 것을 이 호출 순서만으로도 해결이 가능해진다.
        articleIdListRepository.add(payload.getBoardId(), payload.getArticleId(), 1000L);
        boardArticleCountRepository.createOrUpdate(payload.getBoardId(), payload.getBoardArticleCount());
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> event) {
        return ARTICLE_CREATED == event.getType();
    }
}
