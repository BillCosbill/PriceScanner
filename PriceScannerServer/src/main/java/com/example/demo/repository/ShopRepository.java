package com.example.demo.repository;

import com.example.demo.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    boolean existsByUrl(String url);
    Optional<Shop> findByUrl(String url);
}
