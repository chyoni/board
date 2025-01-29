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

    @PostMapping("/articles/{articleId}/users/{userId}")
    public void like(@PathVariable Long articleId, @PathVariable Long userId) {
        articleLikeService.like(articleId, userId);
    }

    @DeleteMapping("/articles/{articleId}/users/{userId}")
    public void unlike(@PathVariable Long articleId, @PathVariable Long userId) {
        articleLikeService.unlike(articleId, userId);
    }
}
