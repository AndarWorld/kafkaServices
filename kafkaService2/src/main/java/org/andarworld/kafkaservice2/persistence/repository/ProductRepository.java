package org.andarworld.kafkaservice2.persistence.repository;

import org.andarworld.kafkaservice2.persistence.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

}
