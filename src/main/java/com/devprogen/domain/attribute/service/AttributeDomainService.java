package com.devprogen.domain.attribute.service;

import com.devprogen.domain.attribute.model.Attribute;
import com.devprogen.infrastructure.persistence.JpaAttributeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AttributeDomainService {
    private final JpaAttributeRepository jpaAttributeRepository;

    // Fetch all attributes
    public List<Attribute> findAll() {
        return jpaAttributeRepository.findAll();
    }

    // Fetch by ID
    public Attribute findById(Long id) {
        return jpaAttributeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attribute not found"));
    }

    // Save attribute
    public Attribute save(Attribute attribute) {
        return jpaAttributeRepository.save(attribute);
    }

    // Delete attribute
    public void delete(Long id) {
        jpaAttributeRepository.deleteById(id);
    }
}
