package com.devprogen.application.generator;

import com.devprogen.application.generator.record.request.GenerateProjectDTO;

public interface GeneratorService {
    Object generateProject(String userName, GenerateProjectDTO generateProjectDTO);
    Object downloadExistingProject(String userName, Long projectId);
}
