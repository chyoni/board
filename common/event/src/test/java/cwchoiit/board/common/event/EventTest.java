package cwchoiit.board.common.event;

import cwchoiit.board.common.event.payload.ArticleCreatedEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class EventTest {

    @Test
    void serde() {
        ArticleCreatedEventPayload payload = ArticleCreatedEventPayload.builder()
                .articleId(1L)
                .title("title")
                .content("content")
                .boardId(1L)
                .writerId(1L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .boardArticleCount(23L)
                .build();

        Event<EventPayload> event = Event.of(1234L, EventType.ARTICLE_CREATED, payload);

        String json = event.toJson();
        log.info("json: {}", json);

        Event<EventPayload> eventInstance = Event.fromJson(json);

        assertThat(eventInstance).isNotNull();
        assertThat(eventInstance.getEventId()).isEqualTo(1234L);
        assertThat(eventInstance.getType()).isEqualTo(event.getType());
        assertThat(eventInstance.getPayload()).isInstanceOf(payload.getClass());

        ArticleCreatedEventPayload resultPayload = (ArticleCreatedEventPayload) eventInstance.getPayload();
        assertThat(resultPayload.getArticleId()).isEqualTo(payload.getArticleId());
        assertThat(resultPayload.getTitle()).isEqualTo(payload.getTitle());
        assertThat(resultPayload.getContent()).isEqualTo(payload.getContent());
        assertThat(resultPayload.getCreatedAt()).isEqualTo(payload.getCreatedAt());
    }
}