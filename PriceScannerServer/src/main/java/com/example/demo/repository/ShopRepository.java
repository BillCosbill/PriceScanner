package com.example.demo.repository;

import com.example.demo.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    boolean existsByUrl(String url);
    Optional<Shop> findByUrl(String url);

    @Query("SELECT c FROM Shop c JOIN FETCH c.productCodes WHERE c.id = (:id)")
    Optional<Shop> findByIdAndFetchCodes(@Param("id") Long id);

    @Query("SELECT c FROM Shop c JOIN FETCH c.productCodes WHERE c.url = (:url)")
    Optional<Shop> findByUrlAndFetchCodes(@Param("url") String url);

    @Query("SELECT c FROM Shop c JOIN FETCH c.errorCodes WHERE c.id = (:id)")
    Optional<Shop> findByIdAndFetchErrorCodes(@Param("id") Long id);

    @Query("SELECT c FROM Shop c JOIN FETCH c.errorCodes WHERE c.url = (:url)")
    Optional<Shop> findByUrlAndFetchErrorCodes(@Param("url") String url);
}
