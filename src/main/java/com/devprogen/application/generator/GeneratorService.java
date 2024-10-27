package com.devprogen.application.generator;

import com.devprogen.application.generator.record.request.GenerateProjectDTO;
import org.springframework.core.io.InputStreamResource;

public interface GeneratorService {
    InputStreamResource generateProject(GenerateProjectDTO generateProjectDTO);
}
