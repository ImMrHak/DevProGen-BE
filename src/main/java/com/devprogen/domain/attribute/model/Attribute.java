package com.devprogen.domain.attribute.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attributes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAttribute;

    private String name;
    private String type;

    private boolean isPrimaryKey = false;
    private boolean isForeignKey = false;

    @Column(nullable = true)
    private String referencedKey;
    @Column(nullable = true)
    private String relationShipType;

    @ManyToOne @JoinColumn
    private com.devprogen.domain.entity.model.Entity entity;
}
