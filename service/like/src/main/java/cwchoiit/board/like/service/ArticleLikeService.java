package cwchoiit.board.like.service;

import cwchoiit.board.common.snowflake.Snowflake;
import cwchoiit.board.like.entity.ArticleLike;
import cwchoiit.board.like.entity.ArticleLikeCount;
import cwchoiit.board.like.repository.ArticleLikeCountRepository;
import cwchoiit.board.like.repository.ArticleLikeRepository;
import cwchoiit.board.like.service.response.ArticleLikeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleLikeService {
    private final Snowflake snowflake = new Snowflake();
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleLikeCountRepository articleLikeCountRepository;

    public ArticleLikeResponse read(Long articleId, Long userId) {
        return articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .map(ArticleLikeResponse::from)
                .orElseThrow();
    }

    // ########################################
    // 비관적 락 (PESSIMISTIC LOCK)
    // ########################################

    /**
     * 비관적 락의 첫번째 방법, update 쿼리를 바로 날림
     * @param articleId articleId
     * @param userId userId
     */
    @Transactional
    public void likePessimisticLockByOne(Long articleId, Long userId) {
        articleLikeRepository.save(ArticleLike.create(snowflake.nextId(), articleId, userId));

        int affectedRecord = articleLikeCountRepository.increase(articleId);
        if (affectedRecord == 0) {
            // 최초 요청시에는 레코드 자체가 없기 때문에 like_count 가 1인 레코드를 직접 집어넣으면 된다.
            // 트래픽이 순식간에 몰리는 경우에는, 이 코드가 여러 요청에서 동시에 실행될 수 있고 그 경우엔 데이터가 유실될 가능성도 있다.
            // 그래서, 게시글 생성 시점에 0으로 미리 초기화 해두는 방법도 있다. 여기서는 그렇게까지는 하지 않겠다.
            // 다른 방법으로는 이 코드 부분만 synchronized 블럭이나 등등의 방법을 사용할 수도 있겠다.
            articleLikeCountRepository.save(ArticleLikeCount.init(articleId, 1L));
        }
    }

    /**
     * 비관적 락의 첫번째 방법, update 쿼리를 바로 날림
     * @param articleId articleId
     * @param userId userId
     */
    @Transactional
    public void unlikePessimisticLockByOne(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleLikeRepository.delete(articleLike);
                    articleLikeCountRepository.decrease(articleId);
                });
    }

    /**
     * 비관적 락의 두번째 방법, select ... for update + update 쿼리
     * @param articleId articleId
     * @param userId userId
     */
    @Transactional
    public void likePessimisticLockByTwo(Long articleId, Long userId) {
        articleLikeRepository.save(ArticleLike.create(snowflake.nextId(), articleId, userId));

        // select ... for update
        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId)
                .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));

        // 현재 like_count + 1
        articleLikeCount.increase();

        // 위 코드에서 findLockedByArticleId 로 레코드를 찾지 못한 경우 최초임을 의미하고 이 최초시에는 init()으로 최초의 상태의 객체를 만들기 때문에
        // 이 객체는 영속성 컨텍스트에 영속된 상태가 아니게 된다. 그 경우에는 save()를 명시적으로 호출해줘야 하므로 아래 save()를 호출
        // 레코드를 찾아서 영속시킨 경우엔 변경감지가 일어나고, 변경감지의 경우엔 약간 애매해지긴 하지만 어쩔수 없다.
        articleLikeCountRepository.save(articleLikeCount);
    }

    /**
     * 비관적 락의 두번째 방법, select ... for update + update 쿼리
     * @param articleId articleId
     * @param userId userId
     */
    @Transactional
    public void unlikePessimisticLockByTwo(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleLikeRepository.delete(articleLike);
                    ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId).orElseThrow();
                    articleLikeCount.decrease();
                });
    }

    // ########################################
    // 낙관적 락 (OPTIMISTIC LOCK)
    // ########################################

    /**
     * 낙관적 락 방법, (@Version 이용)
     * @param articleId articleId
     * @param userId userId
     */
    @Transactional
    public void likeOptimistic(Long articleId, Long userId) {
        articleLikeRepository.save(ArticleLike.create(snowflake.nextId(), articleId, userId));

        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId)
                .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));
        articleLikeCount.increase();
        // 위 코드에서 findById 로 레코드를 찾지 못한 경우 최초임을 의미하고 이 최초시에는 init()으로 최초의 상태의 객체를 만들기 때문에
        // 이 객체는 영속성 컨텍스트에 영속된 상태가 아니게 된다. 그 경우에는 save()를 명시적으로 호출해줘야 하므로 아래 save()를 호출
        // 레코드를 찾아서 영속시킨 경우엔 변경감지가 일어나고, 변경감지의 경우엔 약간 애매해지긴 하지만 어쩔수 없다.
        articleLikeCountRepository.save(articleLikeCount);
    }

    /**
     * 낙관적 락 방법, (@Version 이용)
     * @param articleId articleId
     * @param userId userId
     */
    @Transactional
    public void unlikeOptimistic(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleLikeRepository.delete(articleLike);
                    ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId).orElseThrow();
                    articleLikeCount.decrease();
                });
    }

    public Long count(Long articleId) {
        return articleLikeCountRepository.findById(articleId)
                .map(ArticleLikeCount::getLikeCount)
                .orElse(0L);
    }
}
