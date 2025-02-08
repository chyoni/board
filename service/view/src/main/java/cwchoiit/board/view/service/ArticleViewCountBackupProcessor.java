package cwchoiit.board.view.service;

import cwchoiit.board.common.event.payload.ArticleViewedEventPayload;
import cwchoiit.board.common.outboxmessagerelay.OutboxEventPublisher;
import cwchoiit.board.view.entity.ArticleViewCount;
import cwchoiit.board.view.repository.ArticleViewCountBackupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static cwchoiit.board.common.event.EventType.ARTICLE_VIEWED;

@Component
@RequiredArgsConstructor
public class ArticleViewCountBackupProcessor {
    private final ArticleViewCountBackupRepository articleViewCountBackupRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public void backup(Long articleId, Long viewCount) {
        int affectedRecord = articleViewCountBackupRepository.updateViewCount(articleId, viewCount);
        if (affectedRecord == 0) {
            articleViewCountBackupRepository.findById(articleId)
                    .ifPresentOrElse(
                            ignored -> {
                            },
                            () -> articleViewCountBackupRepository.save(ArticleViewCount.init(articleId, viewCount))
                    );
        }

        // 조회수 이벤트 발행
        outboxEventPublisher.publish(
                ARTICLE_VIEWED,
                ArticleViewedEventPayload.builder()
                        .articleId(articleId)
                        .articleViewCount(viewCount)
                        .build(),
                articleId
        );
    }
}
