package cwchoiit.board.articleread.service.event;

import cwchoiit.board.common.event.Event;
import cwchoiit.board.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}
