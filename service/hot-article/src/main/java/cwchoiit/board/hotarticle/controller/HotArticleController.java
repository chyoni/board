package cwchoiit.board.hotarticle.controller;

import cwchoiit.board.hotarticle.service.HotArticleService;
import cwchoiit.board.hotarticle.service.response.HotArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/hot-articles")
public class HotArticleController {
    private final HotArticleService hotArticleService;

    @GetMapping("/articles/date/{dateStr}")
    public List<HotArticleResponse> readAll(@PathVariable String dateStr) {
        return hotArticleService.readAll(dateStr);
    }
}
