package com.devprogen.domain.project.model;

import com.devprogen.domain.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "entities")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProject;
    private String name;

    @JsonIgnore
    private String projectDirectoryFolder;

    private Date creationDate;

    @ManyToOne
    @JoinColumn
    private User user;

    @JsonIgnore
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "project") @JsonIgnore
    private List<com.devprogen.domain.entity.model.Entity> entities;
}
