package com.example.demo.repository;

import com.example.demo.model.AvailableCode;
import com.example.demo.model.Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvailableCodeRepository extends JpaRepository<AvailableCode, Long> {
    boolean existsByCode(Long code);
    Optional<Code> findByCode(Long code);
    void deleteByCode(Long code);
}
