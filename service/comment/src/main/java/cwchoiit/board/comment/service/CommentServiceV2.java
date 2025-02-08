package cwchoiit.board.comment.service;

import cwchoiit.board.comment.entity.ArticleCommentCount;
import cwchoiit.board.comment.entity.CommentPath;
import cwchoiit.board.comment.entity.CommentV2;
import cwchoiit.board.comment.repository.ArticleCommentCountRepository;
import cwchoiit.board.comment.repository.CommentRepositoryV2;
import cwchoiit.board.comment.service.request.CommentCreateRequestV2;
import cwchoiit.board.comment.service.response.CommentPageResponseV2;
import cwchoiit.board.comment.service.response.CommentResponseV2;
import cwchoiit.board.common.event.payload.CommentCreatedEventPayload;
import cwchoiit.board.common.event.payload.CommentDeletedEventPayload;
import cwchoiit.board.common.outboxmessagerelay.OutboxEventPublisher;
import cwchoiit.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Predicate;

import static cwchoiit.board.common.event.EventType.COMMENT_CREATED;
import static cwchoiit.board.common.event.EventType.COMMENT_DELETED;

@Service
@RequiredArgsConstructor
public class CommentServiceV2 {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepositoryV2 commentRepository;
    private final ArticleCommentCountRepository articleCommentCountRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public CommentResponseV2 create(CommentCreateRequestV2 request) {
        CommentV2 parent = findParent(request);

        // 찾은 상위 댓글이 없는 경우 Root Path 로 Path 객체 생성하고, 있는 경우엔 상위 댓글의 Path 를 받아온다.
        CommentPath parentCommentPath =
                parent == null ? CommentPath.createEmptyPath() : parent.getCommentPath();

        // descendantsTopPath 찾기
        String findDescendantsTopPath =
                commentRepository.findDescendantsTopPath(request.getArticleId(), parentCommentPath.getPath()).orElse(null);

        // 신규 댓글 객체 생성
        CommentV2 newComment = CommentV2.create(
                snowflake.nextId(),
                request.getContent(),
                request.getArticleId(),
                request.getWriterId(),
                parentCommentPath.createChildCommentPath(findDescendantsTopPath)); // 찾은 descendantsTopPath 를 기반으로 하위 댓글을 추가하거나, descendantsTopPath 가 없는 경우, Root 댓글로 생성

        CommentV2 savedNewComment = commentRepository.save(newComment);

        int affectedRecord = articleCommentCountRepository.increase(request.getArticleId());
        if (affectedRecord == 0) {
            articleCommentCountRepository.save(ArticleCommentCount.init(request.getArticleId(), 1L));
        }

        // 댓글 생성 이벤트 발행
        outboxEventPublisher.publish(
                COMMENT_CREATED,
                CommentCreatedEventPayload.builder()
                        .commentId(newComment.getCommentId())
                        .content(newComment.getContent())
                        .articleId(newComment.getArticleId())
                        .writerId(newComment.getWriterId())
                        .markDeleted(newComment.getDeleted())
                        .createdAt(newComment.getCreatedAt())
                        .articleCommentCount(count(newComment.getArticleId()))
                        .build(),
                newComment.getArticleId()
        );

        return CommentResponseV2.from(savedNewComment);
    }

    public CommentResponseV2 read(Long commentId) {
        return CommentResponseV2.from(commentRepository.findById(commentId).orElseThrow());
    }

    public CommentPageResponseV2 readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponseV2.of(
                commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize).stream().map(CommentResponseV2::from).toList(),
                commentRepository.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
        );
    }

    public List<CommentResponseV2> readAllInfinite(Long articleId, Long pageSize, String lastPath) {
        List<CommentV2> comments = lastPath == null ?
                commentRepository.findAllInfinite(articleId, pageSize) :
                commentRepository.findAllInfinite(articleId, pageSize, lastPath);

        return comments.stream()
                .map(CommentResponseV2::from)
                .toList();
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(Predicate.not(CommentV2::getDeleted))
                .ifPresent(comment -> {
                    if (hasChildren(comment)) {
                        comment.markDeleted();
                    } else {
                        deleteComment(comment);
                    }

                    // 댓글 삭제 이벤트 발행
                    outboxEventPublisher.publish(
                            COMMENT_DELETED,
                            CommentDeletedEventPayload.builder()
                                    .commentId(comment.getCommentId())
                                    .content(comment.getContent())
                                    .articleId(comment.getArticleId())
                                    .writerId(comment.getWriterId())
                                    .markDeleted(comment.getDeleted())
                                    .createdAt(comment.getCreatedAt())
                                    .articleCommentCount(count(comment.getArticleId()))
                                    .build(),
                            comment.getArticleId()
                    );
                });
    }

    private boolean hasChildren(CommentV2 comment) {
        // 자신의 path 를 parentPath 로 하여 descendantsTopPath 구했을 때, 존재하면 허위 댓글이 있는것이고 존재하지 않으면 하위 댓글이 없는것
        return commentRepository.findDescendantsTopPath(comment.getArticleId(), comment.getCommentPath().getPath())
                .isPresent();
    }

    private void deleteComment(CommentV2 comment) {
        commentRepository.delete(comment);
        articleCommentCountRepository.decrease(comment.getArticleId());
        if (!comment.isRoot()) {
            commentRepository.findByPath(comment.getCommentPath().getParentPath())
                    .filter(CommentV2::getDeleted)
                    .filter(Predicate.not(this::hasChildren))
                    .ifPresent(this::deleteComment);
        }
    }

    /**
     * 댓글 생성 요청으로부터 받은 parentPath 값을 통해 상위 댓글을 찾는다.
     *
     * @param request 댓글 생성 요청 데이터
     * @return 상위 댓글이 있는 경우 {@link CommentV2}, 없는 경우 {@code null}
     */
    private CommentV2 findParent(CommentCreateRequestV2 request) {
        String parentPath = request.getParentPath();
        if (parentPath == null) {
            return null;
        }
        return commentRepository.findByPath(parentPath) // path 를 통해 상위 댓글을 찾음
                .filter(Predicate.not(CommentV2::getDeleted)) // 상위 댓글이 삭제 마킹이 되지 않아야 하므로 필터링
                .orElseThrow(); // 그게 아닌 경우 에러를 반환
    }

    public Long count(Long articleId) {
        return articleCommentCountRepository.findById(articleId)
                .map(ArticleCommentCount::getCommentCount)
                .orElse(0L);
    }
}
