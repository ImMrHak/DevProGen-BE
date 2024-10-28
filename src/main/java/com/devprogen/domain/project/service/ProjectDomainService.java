package com.devprogen.domain.project.service;

import com.devprogen.domain.project.model.Project;
import com.devprogen.infrastructure.persistence.JpaProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProjectDomainService {
    private final JpaProjectRepository jpaProjectRepository;

    // Fetch all attributes
    public List<Project> findAll() {
        return jpaProjectRepository.findAll();
    }

    // Fetch by ID
    public Project findById(Long id) {
        return jpaProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attribute not found"));
    }

    // Save attribute
    public Project save(Project project) {
        return jpaProjectRepository.save(project);
    }

    // Delete attribute
    public void delete(Long id) {
        jpaProjectRepository.deleteById(id);
    }

    public Long countByUser_UserNameAndIsDeleted(String userName, boolean deleted) {
        return jpaProjectRepository.countByUser_UserNameAndIsDeleted(userName, deleted);
    }

    public List<Project> findAllByUser_UserNameAndIsDeleted(String userName, boolean deleted){
        return jpaProjectRepository.findAllByUser_UserNameAndIsDeleted(userName, deleted);
    }
}
