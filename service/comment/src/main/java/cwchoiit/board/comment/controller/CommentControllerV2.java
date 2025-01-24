package cwchoiit.board.comment.controller;

import cwchoiit.board.comment.service.CommentService;
import cwchoiit.board.comment.service.CommentServiceV2;
import cwchoiit.board.comment.service.request.CommentCreateRequest;
import cwchoiit.board.comment.service.request.CommentCreateRequestV2;
import cwchoiit.board.comment.service.response.CommentPageResponse;
import cwchoiit.board.comment.service.response.CommentPageResponseV2;
import cwchoiit.board.comment.service.response.CommentResponse;
import cwchoiit.board.comment.service.response.CommentResponseV2;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/comments")
public class CommentControllerV2 {
    private final CommentServiceV2 commentService;

    @GetMapping("/{commentId}")
    public CommentResponseV2 read(@PathVariable Long commentId) {
        return commentService.read(commentId);
    }

    @GetMapping
    public CommentPageResponseV2 readAll(@RequestParam Long articleId,
                                         @RequestParam Long page,
                                         @RequestParam Long pageSize) {
        return commentService.readAll(articleId, page, pageSize);
    }

    @GetMapping("/infinite")
    public List<CommentResponseV2> readAllInfinite(@RequestParam Long articleId,
                                                 @RequestParam(required = false) String lastPath,
                                                 @RequestParam Long limit) {
        return commentService.readAllInfinite(articleId, limit, lastPath);
    }

    @PostMapping
    public CommentResponseV2 create(@RequestBody CommentCreateRequestV2 request) {
        return commentService.create(request);
    }

    @DeleteMapping("/{commentId}")
    public void delete(@PathVariable Long commentId) {
        commentService.delete(commentId);
    }
}
