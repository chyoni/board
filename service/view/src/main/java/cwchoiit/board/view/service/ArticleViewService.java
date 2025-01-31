package cwchoiit.board.view.service;

import cwchoiit.board.view.repository.ArticleViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleViewService {
    private static final int BACK_UP_BATCH_SIZE = 100;

    private final ArticleViewCountRepository articleViewCountRepository;
    private final ArticleViewCountBackupProcessor articleViewCountBackupProcessor;

    public Long increase(Long articleId, Long userId) {
        Long count = articleViewCountRepository.increase(articleId);
        if (count % BACK_UP_BATCH_SIZE == 0) {
            articleViewCountBackupProcessor.backup(articleId, count);
        }
        return count;
    }

    public Long count(Long articleId) {
        return articleViewCountRepository.read(articleId);
    }
}
