package com.devprogen.application.entity;

import com.devprogen.domain.entity.model.Entity;
import com.devprogen.domain.entity.service.EntityDomainService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EntityServiceImpl implements EntityService{
    private final EntityDomainService entityDomainService;

    @Override
    public Entity createMyEntity(Entity entity) {
        return entityDomainService.save(entity);
    }
}
