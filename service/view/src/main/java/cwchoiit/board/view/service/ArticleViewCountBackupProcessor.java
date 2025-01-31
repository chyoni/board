package cwchoiit.board.view.service;

import cwchoiit.board.view.entity.ArticleViewCount;
import cwchoiit.board.view.repository.ArticleViewCountBackupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ArticleViewCountBackupProcessor {
    private final ArticleViewCountBackupRepository articleViewCountBackupRepository;

    @Transactional
    public void backup(Long articleId, Long viewCount) {
        int affectedRecord = articleViewCountBackupRepository.updateViewCount(articleId, viewCount);
        if (affectedRecord == 0) {
            articleViewCountBackupRepository.findById(articleId)
                    .ifPresentOrElse(
                            ignored -> {},
                            () -> articleViewCountBackupRepository.save(ArticleViewCount.init(articleId, viewCount))
                    );
        }
    }
}
