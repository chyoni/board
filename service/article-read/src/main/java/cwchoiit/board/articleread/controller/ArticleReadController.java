package cwchoiit.board.articleread.controller;

import cwchoiit.board.articleread.service.ArticleReadService;
import cwchoiit.board.articleread.service.response.ArticleReadPageResponse;
import cwchoiit.board.articleread.service.response.ArticleReadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/articles")
public class ArticleReadController {
    private final ArticleReadService articleReadService;

    @GetMapping("/{articleId}")
    public ArticleReadResponse read(@PathVariable Long articleId) {
        return articleReadService.read(articleId);
    }

    @GetMapping
    public ArticleReadPageResponse readAll(@RequestParam("boardId") Long boardId,
                                           @RequestParam("page") Long page,
                                           @RequestParam("pageSize") Long pageSize) {
        return articleReadService.readAll(boardId, page, pageSize);
    }

    @GetMapping("/infinite")
    public List<ArticleReadResponse> readAllInfinite(@RequestParam("boardId") Long boardId,
                                                     @RequestParam(value = "lastArticleId", required = false) Long lastArticleId,
                                                     @RequestParam("pageSize") Long pageSize) {
        return articleReadService.readAllInfinite(boardId, lastArticleId, pageSize);
    }
}
