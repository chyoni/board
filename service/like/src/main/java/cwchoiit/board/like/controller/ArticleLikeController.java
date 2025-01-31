package cwchoiit.board.like.controller;

import cwchoiit.board.like.service.ArticleLikeService;
import cwchoiit.board.like.service.response.ArticleLikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/article-like")
public class ArticleLikeController {
    private final ArticleLikeService articleLikeService;

    @GetMapping("/articles/{articleId}/users/{userId}")
    public ArticleLikeResponse read(@PathVariable Long articleId, @PathVariable Long userId) {
        return articleLikeService.read(articleId, userId);
    }

    @GetMapping("/articles/{articleId}/count")
    public Long count(@PathVariable Long articleId) {
        return articleLikeService.count(articleId);
    }

    // ##############################
    // 비관적 락 1, 2번 방식
    // ##############################
    @PostMapping("/articles/{articleId}/users/{userId}/pessimistic-lock-1")
    public void likePessimisticLockOne(@PathVariable Long articleId, @PathVariable Long userId) {
        articleLikeService.likePessimisticLockByOne(articleId, userId);
    }

    @DeleteMapping("/articles/{articleId}/users/{userId}/pessimistic-lock-1")
    public void unlikePessimisticLockOne(@PathVariable Long articleId, @PathVariable Long userId) {
        articleLikeService.unlikePessimisticLockByOne(articleId, userId);
    }

    @PostMapping("/articles/{articleId}/users/{userId}/pessimistic-lock-2")
    public void likePessimisticLockTwo(@PathVariable Long articleId, @PathVariable Long userId) {
        articleLikeService.likePessimisticLockByTwo(articleId, userId);
    }

    @DeleteMapping("/articles/{articleId}/users/{userId}/pessimistic-lock-2")
    public void unlikePessimisticLockTwo(@PathVariable Long articleId, @PathVariable Long userId) {
        articleLikeService.unlikePessimisticLockByTwo(articleId, userId);
    }

    // ##############################
    // 낙관적 락 방식
    // ##############################
    @PostMapping("/articles/{articleId}/users/{userId}/optimistic-lock")
    public void likeOptimisticLock(@PathVariable Long articleId, @PathVariable Long userId) {
        articleLikeService.likeOptimistic(articleId, userId);
    }

    @DeleteMapping("/articles/{articleId}/users/{userId}/optimistic-lock")
    public void unlikeOptimisticLock(@PathVariable Long articleId, @PathVariable Long userId) {
        articleLikeService.unlikeOptimistic(articleId, userId);
    }
}
