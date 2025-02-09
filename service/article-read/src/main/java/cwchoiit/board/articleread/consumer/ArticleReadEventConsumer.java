package cwchoiit.board.articleread.consumer;

import cwchoiit.board.articleread.service.ArticleReadService;
import cwchoiit.board.common.event.Event;
import cwchoiit.board.common.event.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static cwchoiit.board.common.event.EventType.Topic.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleReadEventConsumer {
    private final ArticleReadService articleReadService;

    @KafkaListener(topics = {BOARD_ARTICLE, BOARD_COMMENT, BOARD_LIKE})
    public void listen(String message, Acknowledgment ack) {
        log.info("[listen] message: {}", message);
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            articleReadService.handleEvent(event);
        }
        ack.acknowledge();
    }
}
