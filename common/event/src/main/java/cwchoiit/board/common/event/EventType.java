package cwchoiit.board.common.event;

import cwchoiit.board.common.event.payload.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    ARTICLE_CREATED(ArticleCreatedEventPayload.class, Topic.BOARD_ARTICLE),
    ARTICLE_UPDATED(ArticleUpdatedEventPayload.class, Topic.BOARD_ARTICLE),
    ARTICLE_DELETED(ArticleDeletedEventPayload.class, Topic.BOARD_ARTICLE),
    COMMENT_DELETED(CommentDeletedEventPayload.class, Topic.BOARD_COMMENT),
    COMMENT_CREATED(CommentCreatedEventPayload.class, Topic.BOARD_COMMENT),
    ARTICLE_LIKED(ArticleLikedEventPayload.class, Topic.BOARD_LIKE),
    ARTICLE_DISLIKED(ArticleDislikedEventPayload.class, Topic.BOARD_LIKE),
    ARTICLE_VIEWED(ArticleViewedEventPayload.class, Topic.BOARD_VIEW);

    private final Class<? extends EventPayload> payloadClass;
    private final String topic;

    public static EventType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            log.error("[EventType] type = {}", type, e);
            return null;
        }
    }

    private static class Topic {
        private static final String BOARD_ARTICLE = "board-article";
        private static final String BOARD_COMMENT = "board-comment";
        private static final String BOARD_LIKE = "board-like";
        private static final String BOARD_VIEW = "board-view";
    }
}
