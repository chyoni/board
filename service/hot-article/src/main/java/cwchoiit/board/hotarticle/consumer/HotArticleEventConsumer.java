package cwchoiit.board.hotarticle.consumer;

import cwchoiit.board.common.event.Event;
import cwchoiit.board.common.event.EventPayload;
import cwchoiit.board.hotarticle.service.HotArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static cwchoiit.board.common.event.EventType.Topic.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotArticleEventConsumer {

    private final HotArticleService hotArticleService;

    @KafkaListener(topics = {BOARD_ARTICLE, BOARD_COMMENT, BOARD_LIKE, BOARD_VIEW})
    public void listen(String message, Acknowledgment ack) {
        log.info("[listen] received message: {}", message);
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            hotArticleService.handleEvent(event);
        }
        ack.acknowledge();
    }
}
