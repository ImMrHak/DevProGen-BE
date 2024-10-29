package com.devprogen.application.generator;

import com.devprogen.application.attribute.AttributeService;
import com.devprogen.application.entity.EntityService;
import com.devprogen.application.generator.record.request.GenerateProjectDTO;
import com.devprogen.application.generator.record.response.GeneratedProjectDTO;
import com.devprogen.application.project.ProjectService;
import com.devprogen.application.user.UserService;
import com.devprogen.domain.generator.backEnd.BackEndGenerator;
import com.devprogen.domain.generator.frontEnd.FrontEndGenerator;
import com.devprogen.domain.project.model.Project;
import com.devprogen.domain.user.model.User;
import com.devprogen.infrastructure.utility.Utility;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
@AllArgsConstructor
public class GeneratorServiceImpl implements GeneratorService {
    private final UserService userService;
    private final ProjectService projectService;
    private final EntityService entityService;
    private final AttributeService attributeService;

    @Override
    public Object generateProject(String userName, GenerateProjectDTO generateProjectDTO) {

        User dbUser = userService.loadUserByUsername(userName);

        Project newProject = projectService.findAllByUser_UserNameAndIsDeleted(dbUser.getUsername(), false).stream().filter(p -> p.getUser().getIdUser().equals(dbUser.getIdUser()) && p.getName().equals(generateProjectDTO.projectName())).findFirst().orElse(null);

        if(newProject != null) return String.format("Project with the name '%s' already exists.", generateProjectDTO.projectName());

        BackEndGenerator backGenerator = new BackEndGenerator(dbUser, generateProjectDTO.projectName(),generateProjectDTO.isBEOnly());

        backGenerator.projectSI = projectService;
        backGenerator.entitySI = entityService;
        backGenerator.attributeSI = attributeService;

        FrontEndGenerator frontGenerator = null;

        if(!generateProjectDTO.isBEOnly()) frontGenerator = new FrontEndGenerator();

        try (InputStream yamlInputStream = generateProjectDTO.file().getInputStream()) {
            String yamlContent = new String(yamlInputStream.readAllBytes());
            File zipFileBackEnd = backGenerator.generateBackEndProject(new ByteArrayInputStream(yamlContent.getBytes()));

            File zipFileFrontEnd = null;

            if(!generateProjectDTO.isBEOnly()) zipFileFrontEnd = frontGenerator.generateFrontEndProject(new ByteArrayInputStream(yamlContent.getBytes()));

            File combinedZipFile = null;
            if(!generateProjectDTO.isBEOnly()) combinedZipFile = Utility.combineZipFiles(zipFileBackEnd, zipFileFrontEnd, "D:\\GeneratedProjects\\" + generateProjectDTO.projectName() + ".zip");

            if(!generateProjectDTO.isBEOnly()) Utility.deleteDirectoryRecursively(zipFileFrontEnd);

            HttpHeaders headers = new HttpHeaders();

            if(!generateProjectDTO.isBEOnly()) headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + combinedZipFile.getName() + "\"");
            else headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileBackEnd.getName() + "\"");

            return new GeneratedProjectDTO(headers, (generateProjectDTO.isBEOnly()) ? zipFileBackEnd.length() : combinedZipFile.length(), MediaType.APPLICATION_OCTET_STREAM, new InputStreamResource(new FileInputStream((generateProjectDTO.isBEOnly()) ? zipFileBackEnd : combinedZipFile)));
        } catch (IOException e) {
            return "Error generating project: " + e.getMessage();
        }
    }

    @Override
    public Object downloadExistingProject(String userName, Long projectId) {
        User dbUser = userService.loadUserByUsername(userName);
        Project existingProject = projectService.searchMyProjectById(projectId);

        if(existingProject == null) return String.format("Project with ID '%s' does not exist.", projectId);

        if (!dbUser.getIdUser().equals(existingProject.getUser().getIdUser()) && !dbUser.isAdmin()) return "You do not have permission to access this project.";

        File existingProjectFile = new File(existingProject.getProjectDirectoryFolder());

        try{
            HttpHeaders headers = new HttpHeaders();

            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + existingProject.getName() + "\"");

            return new GeneratedProjectDTO(headers, existingProjectFile.length(), MediaType.APPLICATION_OCTET_STREAM, new InputStreamResource(new FileInputStream(existingProjectFile)));
        }
        catch (Exception e){
            return "Error downloading existing project: " + e.getMessage();
        }
    }
}
