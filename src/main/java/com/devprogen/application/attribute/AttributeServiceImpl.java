package com.devprogen.application.attribute;

import com.devprogen.domain.attribute.service.AttributeDomainService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AttributeServiceImpl {
    private final AttributeDomainService attributeDomainService;
}
