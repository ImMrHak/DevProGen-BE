package com.devprogen.infrastructure.persistence;

import com.devprogen.domain.attribute.model.Attribute;
import com.devprogen.domain.attribute.repository.AttributeRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaAttributeRepository extends JpaRepository<Attribute, Long>, AttributeRepository {
}
