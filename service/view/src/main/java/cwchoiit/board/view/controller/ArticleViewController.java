package cwchoiit.board.view.controller;

import cwchoiit.board.view.service.ArticleViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/article-view")
public class ArticleViewController {
    private final ArticleViewService articleViewService;

    @PostMapping("/articles/{articleId}/users/{userId}")
    public Long increase(@PathVariable Long articleId, @PathVariable Long userId) {
        return articleViewService.increase(articleId, userId);
    }

    @GetMapping("/articles/{articleId}/count")
    public Long count(@PathVariable Long articleId) {
        return articleViewService.count(articleId);
    }
}
