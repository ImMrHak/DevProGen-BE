package com.devprogen.domain.user.model;

import com.devprogen.domain.project.model.Project;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String userName;

    @JsonIgnore
    @Column(nullable = true)
    private String password;

    private String accountType;

    private Date creationDate;

    @JsonIgnore
    private boolean isAdmin = false;

    @JsonIgnore
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "user") @JsonIgnore
    private List<Project> projects;

    @Override @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>() {};
        authorities.add( (isAdmin) ? new SimpleGrantedAuthority("ROLE_ADMIN") : new SimpleGrantedAuthority("ROLE_USER"));
        return authorities;
    }

    @Override @JsonIgnore
    public String getUsername() {
        return userName;
    }

    @Override @JsonIgnore
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override @JsonIgnore
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override @JsonIgnore
    public boolean isEnabled() {
        return isDeleted;
    }
}
