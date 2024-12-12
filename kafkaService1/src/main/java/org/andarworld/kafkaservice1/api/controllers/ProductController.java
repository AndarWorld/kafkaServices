package org.andarworld.kafkaservice1.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.andarworld.kafkaservice1.usecases.ProductService;
import org.andarworld.kafkaservice1.usecases.dto.ProductRequestDto;
import org.andarworld.kafkaservice1.usecases.dto.ProductResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product")
@Slf4j
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody ProductRequestDto productRequestDto) {
        log.info("Create order with dto: {}", productRequestDto);
        String messageId = productService.createProduct(productRequestDto);
        return ResponseEntity.ok(messageId);
    }

    @GetMapping("{messageId}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable("messageId") String messageId) {
        log.info("Get product with messageId: {}", messageId);
        return null;
    }
}
