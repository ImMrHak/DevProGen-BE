package com.devprogen.application.entity;

import com.devprogen.domain.entity.service.EntityDomainService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EntityServiceImpl {
    private final EntityDomainService entityDomainService;
}