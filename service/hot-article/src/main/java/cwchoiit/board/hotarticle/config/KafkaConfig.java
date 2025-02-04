package cwchoiit.board.hotarticle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
public class KafkaConfig {

    /**
     * {@link ConcurrentKafkaListenerContainerFactory}는, Kafka Listener 를 생성하고, 병렬 처리를 어떻게 할지 설정한다.
     *
     * @param consumerFactory Kafka Consumer 인스턴스를 생성할 때 필요한 설정들을 담고 있는 팩토리객체이다.
     * @return {@link ConcurrentKafkaListenerContainerFactory}
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
        // 여기서 생성된 factory 를 통해 실제 Kafka Listener(컨테이너)들이 만들어지고 동작하게 된다.
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

        // 위에서 파라미터로 받은 ConsumerFactory 를 factory 에 등록합니다.
        // 이 설정을 통해 Kafka Listener 가 consumerFactory 를 사용하여 Kafka Consumer 를 생성하고, 필요한 Kafka 설정들을 가져갈 수 있습니다.
        factory.setConsumerFactory(consumerFactory);

        // Kafka Consumer 의 오프셋 커밋(Acknowledgement) 모드를 수동(MANUAL)으로 설정합니다.
        // 즉, 자동 커밋(오토 커밋)을 끄고, 개발자가 원하는 시점에 직접 오프셋 커밋을 수행할 수 있게 됩니다.
        // 일반적으로 메시지를 완벽하게 처리한 후에 수동으로 오프셋을 커밋함으로써, 처리가 완료되지 않은 메시지에 대한 재처리 등을 좀 더 세밀하게 제어할 수 있습니다.
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }
}
