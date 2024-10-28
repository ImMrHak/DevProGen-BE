package com.devprogen.adapter.web.UserActions;

import com.devprogen.adapter.wrapper.ResponseWrapper;
import com.devprogen.application.generator.GeneratorService;
import com.devprogen.application.generator.record.request.GenerateProjectDTO;
import com.devprogen.application.generator.record.response.GeneratedProjectDTO;
import com.devprogen.application.project.ProjectService;
import com.devprogen.application.project.record.request.ProjectUpdateNameDTO;
import com.devprogen.application.user.UserService;
import com.devprogen.application.user.record.request.UserUpdateProfileDTO;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final ProjectService projectService;
    private final GeneratorService generatorService;
    //private final LogService logService;

    @GetMapping("/userInfo") @PreAuthorize("isAuthenticated() && hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getUserInfo(Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseWrapper.error("User is not authenticated"));

        return ResponseEntity.status(HttpStatus.OK).body(ResponseWrapper.success(userService.userDataInfo(principal.getName())));
    }

    @GetMapping("/countMyProjects") @PreAuthorize("isAuthenticated() && hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getCountTotalMyProjects(Principal principal) {
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseWrapper.error("User is not authenticated"));

        return ResponseEntity.status(HttpStatus.OK).body(ResponseWrapper.success(projectService.countByUser_UserNameAndIsDeleted(principal.getName(), false)));
    }

    @GetMapping("/listMyProjects") @PreAuthorize("isAuthenticated() && hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getListMyProjects(Principal principal){
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseWrapper.error("User is not authenticated"));

        if(userService.loadUserByUsername(principal.getName()).isAdmin())
            return ResponseEntity.status(HttpStatus.OK).body(ResponseWrapper.success(projectService.findAllProjects()));

        return ResponseEntity.status(HttpStatus.OK).body(ResponseWrapper.success(projectService.findAllByUser_UserNameAndIsDeleted(principal.getName(), false)));
    }

    @PutMapping("/updateMyProjectName") @PreAuthorize("isAuthenticated() && hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> updateMyProjectDetails(Principal principal, @RequestBody ProjectUpdateNameDTO projectUpdateNameDTO){
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseWrapper.error("User is not authenticated"));

        return ResponseEntity.status(HttpStatus.OK).body(ResponseWrapper.success(projectService.updateMyProjectName(projectUpdateNameDTO, principal.getName())));
    }

    @DeleteMapping("/deleteMyProject") @PreAuthorize("isAuthenticated() && hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> deleteMyProject(Principal principal, @RequestParam("projectId") Long projectId){
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseWrapper.error("User is not authenticated"));

        return ResponseEntity.status(HttpStatus.OK).body(ResponseWrapper.success(projectService.deleteMyProject(principal.getName(), projectId)));
    }

    @PostMapping(value = "/generateMyProject", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize("isAuthenticated() && hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> generateMyProject(Principal principal, @RequestParam("file") MultipartFile file, @RequestParam("projectName") String projectName, @RequestParam("isBEOnly") Boolean isBEOnly) {
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseWrapper.error("User is not authenticated"));

        Object data = generatorService.generateProject(principal.getName(), new GenerateProjectDTO(file, projectName, isBEOnly));
        if(data instanceof String) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseWrapper.error((String) data));

        return ResponseEntity.status(HttpStatus.OK).headers(((GeneratedProjectDTO)data).headers()).contentLength(((GeneratedProjectDTO)data).contentLength()).contentType(((GeneratedProjectDTO)data).mediaType()).body(((GeneratedProjectDTO)data).projectGenerated());
    }

    @GetMapping("/downloadMyExistingProject") @PreAuthorize("isAuthenticated() && hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> downloadMyExistingProject(Principal principal, @RequestParam("projectId") Long projectId){
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseWrapper.error("User is not authenticated"));

        Object data = generatorService.downloadExistingProject(principal.getName(), projectId);
        if(data instanceof String)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseWrapper.error((String) data));

        return ResponseEntity.status(HttpStatus.OK).headers(((GeneratedProjectDTO)data).headers()).contentLength(((GeneratedProjectDTO)data).contentLength()).contentType(((GeneratedProjectDTO)data).mediaType()).body(((GeneratedProjectDTO)data).projectGenerated());
    }

    @PutMapping("/updateMyProfile") @PreAuthorize("isAuthenticated() && hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> updateMyProfile(Principal principal, @RequestBody UserUpdateProfileDTO userUpdateProfileDTO){
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseWrapper.error("User is not authenticated"));

        return ResponseEntity.status(HttpStatus.OK).body(ResponseWrapper.success(userService.updateMyProfile(principal.getName(), userUpdateProfileDTO)));
    }
}
