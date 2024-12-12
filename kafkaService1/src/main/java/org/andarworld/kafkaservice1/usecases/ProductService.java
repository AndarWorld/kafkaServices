package org.andarworld.kafkaservice1.usecases;

import org.andarworld.kafkaservice1.usecases.dto.ProductRequestDto;

public interface ProductService {
    String createProduct(ProductRequestDto productRequestDto);
}
