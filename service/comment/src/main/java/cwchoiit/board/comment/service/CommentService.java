package cwchoiit.board.comment.service;

import cwchoiit.board.comment.entity.Comment;
import cwchoiit.board.comment.repository.CommentRepository;
import cwchoiit.board.comment.service.request.CommentCreateRequest;
import cwchoiit.board.comment.service.response.CommentPageResponse;
import cwchoiit.board.comment.service.response.CommentResponse;
import cwchoiit.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 계층형 댓글 구조 (최대 2 Depth) 관련 서비스 클래스
 * 아래 그림이 최대 Depth
 * --- Comment 1---| [1 Depth]
 * ----------------|--Comment 2--| [2 Depth]
 * ----------------|--Comment 3--| [2 Depth]
 */
@Service
@RequiredArgsConstructor
public class CommentService {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse create(CommentCreateRequest request) {
        Optional<Comment> parentOptional = findParent(request);

        Comment newComment = commentRepository.save(
                Comment.create(
                        snowflake.nextId(),
                        request.getContent(),
                        parentOptional.map(Comment::getCommentId).orElse(null),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );

        return CommentResponse.from(newComment);
    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(commentRepository.findById(commentId).orElseThrow());
    }

    /**
     * 페이지 번호 방식의 페이징 데이터
     * @param articleId articleId
     * @param page 현재 페이지
     * @param pageSize 한 페이지 당 보여줄 커멘트 개수
     * @return {@code CommentPageResponse}
     */
    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
                commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize).stream()
                        .map(CommentResponse::from)
                        .toList(),
                commentRepository.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
        );
    }

    /**
     * 무한 스크롤 방식의 페이징 데이터
     * @param articleId articleId
     * @param lastParentCommentId 기준점이 되는 lastParentCommentId
     * @param lastCommentId 기준점이 되는 lastCommentId
     * @param limit limit
     * @return {@code List<CommentResponse>}
     */
    public List<CommentResponse> readAllInfinite(Long articleId, Long lastParentCommentId, Long lastCommentId, Long limit) {
        List<Comment> comments = lastParentCommentId == null || lastCommentId == null ?
                commentRepository.findAllInfinite(articleId, limit) : // 1 페이지
                commentRepository.findAllInfinite(articleId, limit, lastParentCommentId, lastCommentId); // 2 페이지 이상
        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(Predicate.not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if (hasChildren(comment)) {
                        comment.markDeleted();
                    } else {
                        deleteComment(comment);
                    }
                });
    }

    private boolean hasChildren(Comment comment) {
        // 최대 2 Depth 에서 대댓글이 있다면 카운트는 2개 왜냐하면, 자식의 parentCommentId(+1), 본인은 본인의 부모가 없으니 parentCommentId = 본인(+1)
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;
    }

    private void deleteComment(Comment comment) {
        commentRepository.delete(comment);
        if (!comment.isRoot()) {
            commentRepository.findById(comment.getParentCommentId())
                    .filter(Comment::getDeleted)
                    .filter(Predicate.not(this::hasChildren))
                    .ifPresent(this::deleteComment);
        }
    }

    private Optional<Comment> findParent(CommentCreateRequest request) {
        Long parentCommentId = request.getParentCommentId();
        if (parentCommentId == null) {
            return Optional.empty();
        }

        // 최대 2 Depth 이므로, parentCommentId로 찾은 Comment == Root
        return commentRepository.findById(parentCommentId)
                .filter(Predicate.not(Comment::getDeleted))
                .filter(Comment::isRoot);
    }
}
