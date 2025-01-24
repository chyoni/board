package cwchoiit.board.comment.data;

import cwchoiit.board.comment.entity.Comment;
import cwchoiit.board.comment.entity.CommentPath;
import cwchoiit.board.comment.entity.CommentV2;
import cwchoiit.board.common.snowflake.Snowflake;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootTest
public class DataInitializerV2 {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    TransactionTemplate transactionTemplate;

    Snowflake snowflake = new Snowflake();
    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

    static final int BULK_INSERT_COUNT = 5000;
    static final int EXECUTE_COUNT = 5000;

    @Test
    void initialize() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < EXECUTE_COUNT; i++) {
            int start = i * BULK_INSERT_COUNT;
            int end = (i + 1) * BULK_INSERT_COUNT;
            executorService.submit(() -> {
                insert(start, end);
                latch.countDown();
                log.debug("latch count: {}", latch.getCount());
            });
        }
        latch.await();
        executorService.shutdown();
    }

    void insert(int start, int end) {
        transactionTemplate.executeWithoutResult(status -> {
            for (int i = start; i < end; i++) {
                CommentV2 comment = CommentV2.create(
                        snowflake.nextId(),
                        "content" + i,
                        1L,
                        1L,
                        toPath(i));
                entityManager.persist(comment);
            }
        });
    }

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int DEPTH_CHUNK_SIZE = 5; // Depth 당 5개의 문자

    CommentPath toPath(int value) {
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < DEPTH_CHUNK_SIZE; i++) {
            path.insert(0, CHARSET.charAt(value % CHARSET.length()));
            value /= CHARSET.length();
        }
        return CommentPath.create(path.toString());
    }
}
