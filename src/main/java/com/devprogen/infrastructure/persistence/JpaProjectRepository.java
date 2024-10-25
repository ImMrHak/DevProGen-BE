package com.devprogen.infrastructure.persistence;

import com.devprogen.domain.project.model.Project;
import com.devprogen.domain.project.repository.ProjectRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProjectRepository extends JpaRepository<Project, Long>, ProjectRepository {
}
