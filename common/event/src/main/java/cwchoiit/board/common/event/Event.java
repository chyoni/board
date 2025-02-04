package cwchoiit.board.common.event;

import cwchoiit.board.common.dataserializer.DataSerializer;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Event<T extends EventPayload> {
    private Long eventId;
    private EventType type;
    private T payload;

    public static Event<EventPayload> of(Long eventId, EventType type, EventPayload payload) {
        Event<EventPayload> event = new Event<>();
        event.eventId = eventId;
        event.type = type;
        event.payload = payload;
        return event;
    }

    /**
     * Event 타입의 클래스를 Json 으로 변환 (직렬화)
     * @return 변환된 Json
     */
    public String toJson() {
        return DataSerializer.serialize(this);
    }

    /**
     * Json 데이터를 Event 클래스 타입으로 변환 (역직렬화)
     * @param json 변환할 Json 데이터
     * @return {@link Event<EventPayload>}
     */
    public static Event<EventPayload> fromJson(String json) {
        EventRaw eventRaw = DataSerializer.deserialize(json, EventRaw.class);
        if (eventRaw == null) {
            return null;
        }
        Event<EventPayload> event = new Event<>();
        event.eventId = eventRaw.getEventId();
        event.type = EventType.from(eventRaw.getType());
        event.payload = DataSerializer.deserialize(eventRaw.getPayload(), Objects.requireNonNull(event.type).getPayloadClass());
        return event;
    }

    @Getter
    private static class EventRaw {
        private Long eventId;
        private String type;
        private Object payload;
    }
}
