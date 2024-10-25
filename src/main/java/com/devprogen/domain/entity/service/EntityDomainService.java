package com.devprogen.domain.entity.service;

import com.devprogen.domain.entity.model.Entity;
import com.devprogen.infrastructure.persistence.JpaEntityRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EntityDomainService {
    private final JpaEntityRepository jpaEntityRepository;

    // Fetch all attributes
    public List<Entity> findAll() {
        return jpaEntityRepository.findAll();
    }

    // Fetch by ID
    public Entity findById(Long id) {
        return jpaEntityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attribute not found"));
    }

    // Save attribute
    public Entity save(Entity entity) {
        return jpaEntityRepository.save(entity);
    }

    // Delete attribute
    public void delete(Long id) {
        jpaEntityRepository.deleteById(id);
    }
}
