package com.devprogen.domain.entity.model;

import com.devprogen.domain.attribute.model.Attribute;
import com.devprogen.domain.project.model.Project;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@jakarta.persistence.Entity
@Table(name = "entities")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "attributes")
@Builder
public class Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEntity;

    private String name;

    private boolean inheritFromUser = false;

    @JsonIgnore
    private boolean isDeleted = false;

    @ManyToOne
    @JoinColumn
    private Project project;

    @OneToMany(mappedBy = "entity") @JsonIgnore
    private List<Attribute> attributes;
}
