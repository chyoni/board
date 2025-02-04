package cwchoiit.board.hotarticle.service;

import cwchoiit.board.common.event.Event;
import cwchoiit.board.common.event.EventPayload;
import cwchoiit.board.hotarticle.client.ArticleClient;
import cwchoiit.board.hotarticle.repository.HotArticleListRepository;
import cwchoiit.board.hotarticle.service.eventhandler.EventHandler;
import cwchoiit.board.hotarticle.service.response.HotArticleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static cwchoiit.board.common.event.EventType.ARTICLE_CREATED;
import static cwchoiit.board.common.event.EventType.ARTICLE_DELETED;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotArticleService {
    private final ArticleClient articleClient;
    private final List<EventHandler<EventPayload>> eventHandlers;
    private final HotArticleScoreUpdater hotArticleScoreUpdater;
    private final HotArticleListRepository hotArticleListRepository;

    public void handleEvent(Event<EventPayload> event) {
        EventHandler<EventPayload> eventHandler = findEventHandler(event);
        if (eventHandler == null) {
            return;
        }

        if (isArticleCreatedOrDeleted(event)) {
            eventHandler.handle(event); // 게시글 생성 및 삭제 이벤트는 점수를 반영하지 않음
        } else {
            hotArticleScoreUpdater.update(event, eventHandler);
        }
    }

    public List<HotArticleResponse> readAll(String dateStr) {
        return hotArticleListRepository.readAll(dateStr).stream()
                .map(articleClient::read)
                .filter(Objects::nonNull)
                .map(HotArticleResponse::from)
                .toList();
    }

    private EventHandler<EventPayload> findEventHandler(Event<EventPayload> event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }

    private boolean isArticleCreatedOrDeleted(Event<EventPayload> event) {
        return ARTICLE_CREATED == event.getType() || ARTICLE_DELETED == event.getType();
    }
}
