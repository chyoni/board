package cwchoiit.board.common.outboxmessagerelay;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@EnableAsync // Kafka 로 이벤트를 전송할 때 비동기적으로 전송할 것이므로
@Configuration
@ComponentScan("cwchoiit.board.common.outboxmessagerelay") // 이 모듈을 가져다가 사용하는 모든 서비스들(게시글, 좋아요, 댓글, 조회수 등)이 여기에 선언한 컴포넌트들을 모두 스캔할 수 있도록
@EnableScheduling // 전송되지 않은 이벤트들도 모두 전송시키기 위해 생성된지 10초가 지난 이벤트들을 주기적으로 polling 하기 위함
public class MessageRelayConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * 이 KafkaTemplate 을 이용하여 Producer 들이 이벤트를 Kafka 로 전송할 수 있다.
     * @return {@code KafkaTemplate<String, String>}
     */
    @Bean
    public KafkaTemplate<String, String> messageRelayKafkaTemplate() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
    }

    /**
     * 이 스레드풀은, 트랜잭션이 정상 커밋된 후 비동기적으로 이벤트를 Kafka 로 보낼 때 사용될 풀이다.
     * @return {@link Executor}
     */
    @Bean
    public Executor messageRelayPublishEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("mr-pub-event-");
        return executor;
    }

    /**
     * 이벤트 전송이 10초가 지나도 되지 않은 것들을 polling 하여 전송하는데 사용될 스레드 풀
     * @return {@link Executor}
     */
    @Bean
    public Executor messageRelayPublishPendingEventExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
