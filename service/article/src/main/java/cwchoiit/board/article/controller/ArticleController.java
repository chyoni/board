package cwchoiit.board.article.controller;

import cwchoiit.board.article.service.ArticleService;
import cwchoiit.board.article.service.request.ArticleCreateRequest;
import cwchoiit.board.article.service.request.ArticleUpdateRequest;
import cwchoiit.board.article.service.response.ArticlePageResponse;
import cwchoiit.board.article.service.response.ArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/articles")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/{articleId}")
    public ArticleResponse read(@PathVariable Long articleId) {
        return articleService.read(articleId);
    }

    @GetMapping
    public ArticlePageResponse readAll(@RequestParam("boardId") Long boardId,
                                       @RequestParam("page") Long page,
                                       @RequestParam("pageSize") Long pageSize) {
        return articleService.readAll(boardId, page, pageSize);
    }

    @GetMapping("/infinite")
    public List<ArticleResponse> readAllInfinite(@RequestParam("boardId") Long boardId,
                                                 @RequestParam("pageSize") Long pageSize,
                                                 @RequestParam(value = "lastArticleId", required = false) Long lastArticleId) {
        return articleService.readAllInfinite(boardId, pageSize, lastArticleId);
    }

    @PostMapping
    public ArticleResponse create(@RequestBody ArticleCreateRequest request) {
        return articleService.create(request);
    }

    @PutMapping("/{articleId}")
    public ArticleResponse update(@PathVariable Long articleId, @RequestBody ArticleUpdateRequest request) {
        return articleService.update(articleId, request);
    }

    @DeleteMapping("/{articleId}")
    public void delete(@PathVariable Long articleId) {
        articleService.delete(articleId);
    }
}
