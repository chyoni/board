package cwchoiit.board.comment.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPath {
    private String path;

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int DEPTH_CHUNK_SIZE = 5; // Depth 당 5개의 문자
    private static final int MAX_DEPTH = 5; // 원래는 무한 Depth, 그러나 공부 목적이므로 최대 5 Depth

    private static final String MIN_CHUNK = String.valueOf(CHARSET.charAt(0)).repeat(DEPTH_CHUNK_SIZE); // 00000, Depth 에서 나타낼 수 있는 가장 작은 문자열
    private static final String MAX_CHUNK = String.valueOf(CHARSET.charAt(CHARSET.length() - 1)).repeat(DEPTH_CHUNK_SIZE); // zzzzz, Depth 에서 나타낼 수 있는 가장 큰 문자열

    /**
     * CommentPath 생성 메서드.
     * @param path path
     * @return {@link CommentPath}
     */
    public static CommentPath create(String path) {
        if (isDepthOverflow(path)) {
            throw new IllegalStateException("Depth overflow");
        }
        CommentPath commentPath = new CommentPath();
        commentPath.path = path;
        return commentPath;
    }

    /**
     * Path 가 없는 CommentPath 객체 생성. 이 객체는 곧 Root 댓글이 될 것.
     * path 가 ""인 상태는 Root 댓글이 아님. 곧 1 Depth 가 될 CommentPath 객체를 편리하게 만들기 위한 메서드.
     * @return {@link CommentPath}
     */
    public static CommentPath createEmptyPath() {
        CommentPath commentPath = new CommentPath();
        commentPath.path = "";
        return commentPath;
    }

    /**
     * Depth 가 5 Depth 이상으로 갈려고 하는지 체크 (원래는 무한 Depth 인데 5 Depth 로 한정)
     * @param path path
     * @return 5 Depth 가 넘어가면 {@code true}, 그렇지 않으면 {@code false}
     */
    private static boolean isDepthOverflow(String path) {
        return calDepth(path) > MAX_DEPTH;
    }

    /**
     * 현재 Depth 를 구한다.
     * 예를 들어, {@code path.length()}가 25일때, {@code DEPTH_CHUNK_SIZE}인 5로 나눈값이 Depth 가 된다.
     * @param path path
     * @return Depth
     */
    private static int calDepth(String path) {
        return path.length() / DEPTH_CHUNK_SIZE;
    }

    /**
     * 현재 Depth 를 구한다.
     * @return 현재 Depth
     */
    public int getDepth() {
        return calDepth(path);
    }

    /**
     * 1 Depth 인지 확인
     * @return 1 Depth 라면 {@code true}, 아니면 {@code false}
     */
    public boolean isRoot() {
        return calDepth(path) == 1;
    }

    /**
     * 현재 path 의 부모 path 를 구한다.
     * 예를 들어, 00000 00001 인 경우, 부모 path = 00000
     * @return 부모 path
     */
    public String getParentPath() {
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE);
    }

    /**
     * 하위 커멘트의 Path 를 생성한다.
     * @param descendantsTopPath descendantsTopPath
     * @return {@link CommentPath}
     */
    public CommentPath createChildCommentPath(String descendantsTopPath) {
        if (descendantsTopPath == null) { // 하위 댓글이 없는 경우
            return CommentPath.create(path + MIN_CHUNK);
        }
        String childrenTopPath = findChildrenTopPath(descendantsTopPath);
        return CommentPath.create(increase(childrenTopPath));
    }

    /**
     * childrenTopPath 를 구한다.
     * childrenTopPath 를 구하는 방법은, descendantsTopPath 를 구한 다음, 이 값에서
     * 신규 댓글의 Depth * 5 개의 문자만 남기고 잘라낸 값이다.
     * @param descendantsTopPath descendantsTopPath
     * @return childrenTopPath
     */
    private String findChildrenTopPath(String descendantsTopPath) {
        return descendantsTopPath.substring(0, (getDepth() + 1) * DEPTH_CHUNK_SIZE);
    }

    /**
     * 현재 path 에 +1 을 추가한다.
     * @param path path
     * @return path+1
     */
    private String increase(String path) {
        String lastChunk = path.substring(path.length() - DEPTH_CHUNK_SIZE);
        if (isChunkOverflow(lastChunk)) {
            throw new IllegalStateException("Chunk overflow");
        }

        int charsetLength = CHARSET.length(); // 62

        // 10진수로 변환한 값을 저장할 변수
        int value = 0;
        // lastChunk(path 의 마지막 5개 문자)를 한 글자씩 순회하면서 10진수로 변경
        for (char c : lastChunk.toCharArray()) {
            value = value * charsetLength + CHARSET.indexOf(c);
        }
        // 10진수로 변환한 lastChunk 값에 + 1
        value = value + 1;

        // +1 결과 10진수를 다시 62진수로 변경
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < DEPTH_CHUNK_SIZE; i++) {
            result.insert(0, CHARSET.charAt(value % charsetLength));
            value /= charsetLength;
        }

        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE) + result;
    }

    /**
     * 새로 만들어낼 Depth 가 이미 00000 - zzzzz 까지 꽉 찬 경우인지 확인
     * @param lastChunk 마지막 5개 문자
     * @return 이미 zzzzz 까지 꽉 찬 경우 {@code true}, 그렇지 않으면 {@code false}
     */
    private boolean isChunkOverflow(String lastChunk) {
        return MAX_CHUNK.equals(lastChunk);
    }
}
