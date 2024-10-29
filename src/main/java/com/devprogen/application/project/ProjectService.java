package com.devprogen.application.project;

import com.devprogen.application.project.record.request.ProjectUpdateNameDTO;
import com.devprogen.domain.project.model.Project;

import java.util.List;

public interface ProjectService {
    Project createMyProject(Project project);
    Long countByUser_UserNameAndIsDeleted(String userName, Boolean deleted);
    List<Project> findAllByUser_UserNameAndIsDeleted(String userName, Boolean deleted);
    List<Project> findAllProjects();
    Project searchMyProjectById(Long projectId);
    String updateMyProjectName(ProjectUpdateNameDTO project, String userName);
    String deleteMyProject(String userName, Long idProject);
    List<Project> findAllByIsDeleted(Boolean deleted);
    String recoverDeletedProject(Long idProject);
}
