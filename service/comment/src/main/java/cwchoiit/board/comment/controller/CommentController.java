package cwchoiit.board.comment.controller;

import cwchoiit.board.comment.service.CommentService;
import cwchoiit.board.comment.service.request.CommentCreateRequest;
import cwchoiit.board.comment.service.response.CommentPageResponse;
import cwchoiit.board.comment.service.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{commentId}")
    public CommentResponse read(@PathVariable Long commentId) {
        return commentService.read(commentId);
    }

    @GetMapping
    public CommentPageResponse readAll(@RequestParam Long articleId,
                                       @RequestParam Long page,
                                       @RequestParam Long pageSize) {
        return commentService.readAll(articleId, page, pageSize);
    }

    @GetMapping("/infinite")
    public List<CommentResponse> readAllInfinite(@RequestParam Long articleId,
                                                 @RequestParam(required = false) Long lastParentCommentId,
                                                 @RequestParam(required = false) Long lastCommentId,
                                                 @RequestParam Long limit) {
        return commentService.readAllInfinite(articleId, lastParentCommentId, lastCommentId, limit);
    }

    @PostMapping
    public CommentResponse create(@RequestBody CommentCreateRequest request) {
        return commentService.create(request);
    }

    @DeleteMapping("/{commentId}")
    public void delete(@PathVariable Long commentId) {
        commentService.delete(commentId);
    }
}
