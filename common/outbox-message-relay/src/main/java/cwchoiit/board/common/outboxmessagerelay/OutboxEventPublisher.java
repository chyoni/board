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

        // Message Relay 가 이 이벤트를 받을 것, 스프링의 ApplicationEventPublisher 를 통해 이벤트를 발행
        // 그러나, 이 이벤트를 발행한다고해서 곧바로 Message Relay 에 선언한 이벤트 리스너가 호출되는 게 아니라 시점에 맞게 호출됨
        // 나의 경우, 커밋 바로 직전과 커밋 직후로 설정해 두었다. 
        eventPublisher.publishEvent(OutboxEvent.of(outbox));
    }
}
