package com.devprogen.domain.project.repository;

import com.devprogen.domain.project.model.Project;

import java.util.List;

public interface ProjectRepository {
    Long countByUser_UserNameAndIsDeleted(String userName, boolean isDeleted);
    List<Project> findAllByUser_UserNameAndIsDeleted(String userName, boolean isDeleted);
}
