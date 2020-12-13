package com.example.demo.repository;

import com.example.demo.model.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErrorCodeRepository extends JpaRepository<ErrorCode, Long> {
    boolean existsByCode(Long code);
    Optional<ErrorCode> findByCode(Long code);
}
