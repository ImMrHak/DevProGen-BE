package com.devprogen.infrastructure.persistence;

import com.devprogen.domain.entity.model.Entity;
import com.devprogen.domain.entity.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaEntityRepository extends JpaRepository<Entity, Long>, EntityRepository {
}
