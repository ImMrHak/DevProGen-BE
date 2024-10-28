package com.devprogen.application.attribute;

import com.devprogen.domain.attribute.model.Attribute;
import com.devprogen.domain.attribute.service.AttributeDomainService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AttributeServiceImpl implements AttributeService {
    private final AttributeDomainService attributeDomainService;

    @Override
    public Attribute createMyAttribute(Attribute attribute) {
        return attributeDomainService.save(attribute);
    }
}
