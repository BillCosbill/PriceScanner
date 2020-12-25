package com.example.demo.repository;

import com.example.demo.model.CodeStatus;
import com.example.demo.model.enums.ECodeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeStatusRepository extends JpaRepository<CodeStatus, Long> {
    Optional<CodeStatus> findByStatus(ECodeStatus status);
    boolean existsByStatus (ECodeStatus status);
}
