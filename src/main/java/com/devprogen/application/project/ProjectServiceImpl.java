package com.devprogen.application.project;

import com.devprogen.domain.project.service.ProjectDomainService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProjectServiceImpl {
    private final ProjectDomainService projectDomainService;
}
