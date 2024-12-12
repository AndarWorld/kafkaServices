package org.andarworld.kafkaservice1.usecases.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.andarworld.core.events.ProductCreateEvent;
import org.andarworld.core.events.ProductResultEvent;
import org.andarworld.kafkaservice1.usecases.dto.ProductResponseDto;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {
        "${kafka.topics.names.product-result-event-topic}",
        "${kafka.topics.names.product-create-event-topic}"
})
@RequiredArgsConstructor
@Slf4j
public class ProductEventsHandler {

    @KafkaHandler
    public void handler(@Payload ProductResultEvent event,
                        @Header("messageId") String messageId) {
        ProductResponseDto productResponseDto =
                new ProductResponseDto(event.name(), event.quantity(),
                        event.price());
        log.info("Product result event: {}", productResponseDto);
        // to do save logic
    }
}
