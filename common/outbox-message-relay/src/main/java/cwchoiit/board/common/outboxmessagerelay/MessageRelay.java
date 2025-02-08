package cwchoiit.board.common.outboxmessagerelay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Outbox 테이블에 전송되지 않은 이벤트가 있는지 주기적으로 Polling 을 하고, 있다면 그 이벤트를 Kafka 로 전송해준다. 또한,
 * 애플리케이션이 트랜잭션이 성공적으로 끝나면 직접적으로 이녀석한테 이벤트를 전달해서
 * 비동기적으로 해당 이벤트를 Kafka 로 전달해주는 중개자 클래스.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRelay {
    private final OutboxRepository outboxRepository;
    private final MessageRelayCoordinator messageRelayCoordinator;
    private final KafkaTemplate<String, String> messageRelayKafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT) // 트랜잭션이 커밋되기 직전 이벤트를 받는다.
    public void createOutbox(OutboxEvent event) {
        log.info("[createOutbox] outBoxEvent = {}", event);
        // 트랜잭션이 커밋되기 전 Outbox 테이블에 레코드를 생성하기 위해 (이벤트를 호출한 곳에서 사용한 트랜잭션 그대로를 사용하는 것)
        outboxRepository.save(event.getOutbox());
    }

    @Async("messageRelayPublishEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 트랜잭션이 커밋된 직후 이벤트를 받는다.
    public void publishEvent(OutboxEvent event) {
        // 커밋 후 애플리케이션으로부터 직접 Message Relay 는 이렇게 이벤트를 받을 수 있고
        // 해당 이벤트를 Kafka 로 비동기적으로 전송한다.
        publishEvent(event.getOutbox());
    }

    private void publishEvent(Outbox outbox) {
        try {
            messageRelayKafkaTemplate.send(
                    outbox.getEventType().getTopic(), // Topic
                    String.valueOf(outbox.getShardKey()), // shard_key 가 같은 것들은 동일한 파티션으로 전송이 된다. 동일한 파티션으로 보내지는 것들은 순서가 보장된다.
                    outbox.getPayload() // payload
            ).get(1, TimeUnit.SECONDS); // get() 하면 결과를 반환받을 수 있음.
            outboxRepository.delete(outbox); // 정상적으로 전송됐으면 우리 비즈니스 정책의 경우 Outbox 에 레코드를 삭제한다.
        } catch (Exception e) {
            log.error("[publishEvent] outbox = {}", outbox, e);
        }
    }

    /**
     * 10초가 지나도 이벤트가 전송되지 않은 것들을 전부 Polling 해서 Kafka 로 전송하는 스케쥴링 메서드.
     */
    @Scheduled(fixedDelay = 10,
            initialDelay = 5,
            timeUnit = TimeUnit.SECONDS,
            scheduler = "messageRelayPublishPendingEventExecutor")
    public void publishPendingEvents() {
        AssignedShard assignedShard = messageRelayCoordinator.assignShards();
        log.debug("[publishPendingEvents] assignedShard = {}", assignedShard.getShards().size());
        for (Long shard : assignedShard.getShards()) {
            List<Outbox> outboxes = outboxRepository.findAllByShardKeyAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
                    shard,
                    LocalDateTime.now().minusSeconds(10), // 현재 시간보다 10초가 지난
                    Pageable.ofSize(100) // 모든것들을 다 조회하지 말고 100개씩만
            );
            for (Outbox outbox : outboxes) {
                publishEvent(outbox);
            }
        }
    }
}
