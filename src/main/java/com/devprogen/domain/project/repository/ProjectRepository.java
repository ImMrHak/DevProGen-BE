package com.devprogen.domain.project.repository;

import com.devprogen.domain.project.model.Project;

public interface ProjectRepository {
    Long countByUser_UserNameAndIsDeleted(String userName, boolean isDeleted);
    Project findAllByUser_UserNameAndIsDeleted(String userName, boolean isDeleted);
}
