package cwchoiit.board.comment.data;

import cwchoiit.board.comment.entity.Comment;
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
public class DataInitializer {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    TransactionTemplate transactionTemplate;

    Snowflake snowflake = new Snowflake();
    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

    static final int BULK_INSERT_COUNT = 2000;
    static final int EXECUTE_COUNT = 2000;

    @Test
    void initialize() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < EXECUTE_COUNT; i++) {
            executorService.submit(() -> {
                insert();
                latch.countDown();
                log.debug("latch count: {}", latch.getCount());
            });
        }
        latch.await();
        executorService.shutdown();
    }

    void insert() {
        transactionTemplate.executeWithoutResult(status -> {
            Comment prev = null;

            for (int i = 0; i < BULK_INSERT_COUNT; i++) {
                Comment comment = Comment.create(
                        snowflake.nextId(),
                        "content" + i,
                        i % 2 == 0 ? null : prev.getCommentId(),
                        1L,
                        1L);

                prev = comment;
                entityManager.persist(comment);
            }
        });
    }
}
