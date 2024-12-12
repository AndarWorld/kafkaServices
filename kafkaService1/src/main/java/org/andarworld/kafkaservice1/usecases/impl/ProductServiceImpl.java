package org.andarworld.kafkaservice1.usecases.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.andarworld.kafkaservice1.usecases.ProductService;
import org.andarworld.kafkaservice1.usecases.dto.ProductRequestDto;
import org.andarworld.core.events.ProductCreateEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Value("${kafka.topics.names.product-create-event-topic}")
    private String topicCreateEventProduct;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public String createProduct(ProductRequestDto productRequestDto) {
        ProductCreateEvent productCreateEvent =
                new ProductCreateEvent(productRequestDto.uuid(),productRequestDto.name(), productRequestDto.quantity());
        String messageId = UUID.randomUUID().toString();

        ProducerRecord<String, Object> record =
                new ProducerRecord<>(topicCreateEventProduct, productCreateEvent);
        record.headers().add("messageId", messageId.getBytes());

        SendResult<String, Object> sendResult;

        try {
           sendResult = kafkaTemplate.send(record).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        log.info("Topic: {}", sendResult.getRecordMetadata().topic());
        log.info("Partition: {}", sendResult.getRecordMetadata().partition());
        log.info("Offset: {}", sendResult.getRecordMetadata().offset());
        log.info("Timestamp: {}", sendResult.getRecordMetadata().timestamp());

        log.info("Result: {}", sendResult.getProducerRecord().toString());
        return messageId;
    }
}
