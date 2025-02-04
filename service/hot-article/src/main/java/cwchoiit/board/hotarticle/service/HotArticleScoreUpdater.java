package cwchoiit.board.hotarticle.service;

import cwchoiit.board.common.event.Event;
import cwchoiit.board.common.event.EventPayload;
import cwchoiit.board.hotarticle.repository.ArticleCreatedTimeRepository;
import cwchoiit.board.hotarticle.repository.HotArticleListRepository;
import cwchoiit.board.hotarticle.service.eventhandler.EventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HotArticleScoreUpdater {
    private static final long HOT_ARTICLE_COUNT = 10;
    private static final Duration HOT_ARTICLE_TTL = Duration.ofDays(10);

    private final HotArticleListRepository hotArticleListRepository;
    private final HotArticleScoreCalculator hotArticleScoreCalculator;
    private final ArticleCreatedTimeRepository articleCreatedTimeRepository;

    public void update(Event<EventPayload> event, EventHandler<EventPayload> eventHandler) {
        Long articleId = eventHandler.findArticleId(event);
        LocalDateTime createdTime = articleCreatedTimeRepository.read(articleId);

        // 좋아요, 조회, 댓글 이벤트는 오늘 게시글이 아니더라도 언제든지 어떤날 생성된 게시글이든 할 수 있는 액션이지만, 오늘 인기글을 구하는 이 서비스는 해당 게시글과 연관이 없어야 하므로
        if (!isArticleCreatedToday(createdTime)) {
            return;
        }

        eventHandler.handle(event);

        long score = hotArticleScoreCalculator.calculate(articleId);

        hotArticleListRepository.add(articleId, createdTime, score, HOT_ARTICLE_COUNT, HOT_ARTICLE_TTL);
    }

    private boolean isArticleCreatedToday(LocalDateTime createdTime) {
        return createdTime != null && createdTime.toLocalDate().equals(LocalDate.now());
    }
}
