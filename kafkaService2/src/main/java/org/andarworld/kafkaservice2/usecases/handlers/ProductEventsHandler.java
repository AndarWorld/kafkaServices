package org.andarworld.kafkaservice2.usecases.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.andarworld.core.events.ProductCreateEvent;
import org.andarworld.core.events.ProductResultEvent;
import org.andarworld.kafkaservice2.api.exceptions.NonRetriableException;
import org.andarworld.kafkaservice2.api.exceptions.RetriableException;
import org.andarworld.kafkaservice2.persistence.model.Product;
import org.andarworld.kafkaservice2.persistence.repository.ProductRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


@Component
@KafkaListener(topics = {
        "${kafka.topics.names.product-create-event-topic}"
})
@RequiredArgsConstructor
@Slf4j
public class ProductEventsHandler {

    private final ProductRepository productJpa;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.names.product-result-event-topic}")
    private String productResultEventTopic;

    @KafkaHandler
    public void handler(@Payload ProductCreateEvent event,
                        @Header("messageId") String messageId) {
        UUID uuid = UUID.fromString(event.uuid());
        Product product = productJpa.getReferenceById(uuid);

        BigDecimal quantity = BigDecimal.valueOf(Integer.parseInt(event.quantity()));
        BigDecimal price = product.getPrice().multiply(quantity); //error here TODO

        //here logic for validate quantity and enable to buy this products

        ProductResultEvent productResultEvent =
                new ProductResultEvent(event.uuid(), event.name(), event.quantity(), price);

        ProducerRecord<String, Object> producerRecord =
                new ProducerRecord<>(productResultEventTopic, productResultEvent.uuid(), productResultEvent);
        producerRecord.headers().add("messageId", messageId.getBytes());

        SendResult<String, Object> result;
        try {
          result = kafkaTemplate.send(producerRecord).get();
        } catch (InterruptedException e) {
            throw new RetriableException("Retry");
        } catch (ExecutionException e) {
            log.error("Error in product create event", e);
            throw new NonRetriableException("Non retry");
        }

        log.info("Topic is {}: ", result.getRecordMetadata().topic());
        log.info("Partition is {}: ", result.getRecordMetadata().partition());
        log.info("Offset is {}: ", result.getRecordMetadata().offset());

        log.info("Result event is: {}", result.getRecordMetadata());
    }
}
