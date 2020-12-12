package com.example.demo.repository;

import com.example.demo.model.Product;
import com.example.demo.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
