package org.andarworld.kafkaservice2.config;

import lombok.RequiredArgsConstructor;
import org.andarworld.kafkaservice2.api.exceptions.NonRetriableException;
import org.andarworld.kafkaservice2.api.exceptions.RetriableException;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    @Value("${kafka.topics.names.product-create-event-topic}")
    private String productCreateEventTopic;
    @Value("${kafka.topics.names.product-result-event-topic}")
    private String productResultEventTopic;

    @Value("${kafka.topics.partitions}")
    private Integer partition;
    @Value("${kafka.topics.replication-factor}")
    private Integer replicationFactor;

    private final Environment env;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                env.getRequiredProperty("spring.kafka.admin.properties.bootstrap.servers")));
    }

    private Map<String, Object> producerConfig() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                env.getRequiredProperty("spring.kafka.producer.bootstrap-servers"));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        config.put(ProducerConfig.ACKS_CONFIG,
                env.getRequiredProperty("spring.kafka.producer.acks"));
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                env.getRequiredProperty("spring.kafka.producer.properties.enable.idempotence"));

        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG,
                env.getRequiredProperty("spring.kafka.producer.properties.delivery.timeout.ms"));
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                env.getRequiredProperty("spring.kafka.producer.properties.request.timeout.ms"));
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                env.getRequiredProperty("spring.kafka.producer.properties.max.in.flight.requests.per.connection"));

        config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG,
                env.getRequiredProperty("spring.kafka.producer.transaction-id-prefix"));

        return config;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTransactionManager<String, Object> kafkaTransactionManager(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    private Map<String, Object> consumerConfig() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                env.getRequiredProperty("spring.kafka.consumer.bootstrap-servers"));

        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        config.put(JsonDeserializer.TRUSTED_PACKAGES,
                env.getRequiredProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"));

        config.put(ConsumerConfig.GROUP_ID_CONFIG, env.getRequiredProperty("spring.kafka.consumer.group-id"));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_DOC,
                env.getRequiredProperty("spring.kafka.consumer.auto-offset-reset"));
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG,
                env.getRequiredProperty("spring.kafka.consumer.isolation-level"));

        return config;
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory, KafkaTemplate<String, Object> kafkaTemplate
    ) {
        DefaultErrorHandler defaultErrorHandler =
                new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate),
                        new FixedBackOff(2000, 3));
        defaultErrorHandler.addRetryableExceptions(RetriableException.class);
        defaultErrorHandler.addNotRetryableExceptions(NonRetriableException.class);

        ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory);
        kafkaListenerContainerFactory.setCommonErrorHandler(defaultErrorHandler);

        return kafkaListenerContainerFactory;
    }

    @Bean
    public NewTopic productCreateEventTopic() {
        return TopicBuilder
                .name(productCreateEventTopic)
                .partitions(partition)
                .replicas(replicationFactor)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, getReplicationFactor())
                .build();

    }

    @Bean
    public NewTopic productResultEventTopic() {
        return TopicBuilder
                .name(productResultEventTopic)
                .partitions(partition)
                .replicas(replicationFactor)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, getReplicationFactor())
                .build();
    }

    private String getReplicationFactor() {
        if (replicationFactor > 0) {
            return (replicationFactor <= 2) ? String.valueOf(replicationFactor) : String.valueOf(replicationFactor - 1);
        } else throw new IllegalArgumentException("Invalid replication factor, it need be more than 0");
    }
}
