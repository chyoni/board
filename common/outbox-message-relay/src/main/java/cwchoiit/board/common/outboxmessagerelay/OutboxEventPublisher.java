package cwchoiit.board.common.outboxmessagerelay;

import cwchoiit.board.common.event.Event;
import cwchoiit.board.common.event.EventPayload;
import cwchoiit.board.common.event.EventType;
import cwchoiit.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {
    private final Snowflake outboxIdSnowflake = new Snowflake();
    private final Snowflake eventIdSnowflake = new Snowflake();
    private final ApplicationEventPublisher eventPublisher;

    public void publish(EventType eventType, EventPayload payload, Long shardKey) {
        // 물리적 샤드 개수 MessageRelayConstants.SHARD_COUNT (4)
        // 게시글 서비스의 경우 articleId == shard_key
        // shard_key % MessageRelayConstants.SHARD_COUNT 의 결과가 저장될 샤드 번호
        Outbox outbox = Outbox.create(
                outboxIdSnowflake.nextId(),
                eventType,
                Event.of(eventIdSnowflake.nextId(), eventType, payload).toJson(),
                shardKey % MessageRelayConstants.SHARD_COUNT
        );
        eventPublisher.publishEvent(OutboxEvent.of(outbox)); // Message Relay 가 이 이벤트를 받을 것
    }
}
