package com.devprogen.domain.log.service;

import com.devprogen.domain.log.model.Log;
import com.devprogen.infrastructure.persistence.JpaLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class LogDomainService {
    private final JpaLogRepository jpaLogRepository;

    // Fetch all attributes
    public List<Log> findAll() {
        return jpaLogRepository.findAll();
    }

    // Fetch by ID
    public Log findById(Long id) {
        return jpaLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attribute not found"));
    }

    // Save attribute
    public Log save(Log log) {
        return jpaLogRepository.save(log);
    }

    // Delete attribute
    public void delete(Long id) {
        jpaLogRepository.deleteById(id);
    }

}
