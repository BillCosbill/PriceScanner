package com.example.demo.repository;

import com.example.demo.model.Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface CodeRepository extends JpaRepository<Code, Long> {
    boolean existsByCodeValue(Long code);
    Optional<Code> findByCodeValue(Long code);
}
