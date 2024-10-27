package com.devprogen.application.generator;

import com.devprogen.application.attribute.AttributeService;
import com.devprogen.application.entity.EntityService;
import com.devprogen.application.generator.record.request.GenerateProjectDTO;
import com.devprogen.application.project.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GeneratorServiceImpl implements GeneratorService {
    private final ProjectService projectService;
    private final EntityService entityService;
    private final AttributeService attributeService;

    @Override
    public InputStreamResource generateProject(GenerateProjectDTO generateProjectDTO) {
        return null;
    }
}
