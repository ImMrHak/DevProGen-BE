package com.devprogen.application.project;

import com.devprogen.application.project.record.request.ProjectUpdateNameDTO;
import com.devprogen.domain.project.model.Project;
import com.devprogen.domain.project.service.ProjectDomainService;
import com.devprogen.domain.user.service.UserDomainService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProjectServiceImpl implements ProjectService{
    private final ProjectDomainService projectDomainService;
    private final UserDomainService userDomainService;

    @Override
    public Project createMyProject(Project project) {
        return projectDomainService.save(project);
    }

    @Override
    public Long countByUser_UserNameAndIsDeleted(String userName, Boolean deleted) {
        return projectDomainService.countByUser_UserNameAndIsDeleted(userName, deleted);
    }

    @Override
    public List<Project> findAllByUser_UserNameAndIsDeleted(String userName, Boolean deleted){
        return projectDomainService.findAllByUser_UserNameAndIsDeleted(userName, deleted);
    }

    @Override
    public List<Project> findAllProjects() {
        return projectDomainService.findAll().stream().filter(p -> !p.isDeleted()).toList();
    }

    @Override
    public Project searchMyProjectById(Long projectId) {
        return projectDomainService.findById(projectId);
    }

    @Override
    public String updateMyProjectName(ProjectUpdateNameDTO project, String userName) {
        Project dbProject = projectDomainService.findById(project.idProject());

        if(dbProject == null) return "Project not found.";

        if(!dbProject.getUser().getUsername().equals(userName) && !userDomainService.loadUserByUsername(userName).isAdmin()) return "Unauthorized access.";

        dbProject.setName(project.name());

        projectDomainService.save(dbProject);
        return "Project name updated successfully.";
    }

    @Override
    public String deleteMyProject(String userName, Long idProject) {
        Project dbProject = projectDomainService.findById(idProject);

        if(dbProject == null) return "Project not found.";

        if(!dbProject.getUser().getUsername().equals(userName) && !userDomainService.loadUserByUsername(userName).isAdmin()) return "Unauthorized access.";

        dbProject.setDeleted(true);

        projectDomainService.save(dbProject);
        return "Project deleted successfully.";
    }
}
