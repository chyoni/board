package cwchoiit.board.common.outboxmessagerelay;

import cwchoiit.board.common.event.EventType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 각 서비스(게시글, 좋아요, 댓글, 조회수)의 데이터베이스에 추가될 Outbox 테이블
 */
@Table(name = "outbox")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox {
    @Id
    private Long outboxId;
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    private String payload;
    private Long shardKey;
    private LocalDateTime createdAt;

    public static Outbox create(Long outboxId, EventType eventType, String payload, Long shardKey) {
        Outbox outbox = new Outbox();
        outbox.outboxId = outboxId;
        outbox.eventType = eventType;
        outbox.payload = payload;
        outbox.shardKey = shardKey;
        outbox.createdAt = LocalDateTime.now();
        return outbox;
    }
}
