package com.devprogen.domain.user.projection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface UserSignUpProjection {
    Long getIdUser();
    String getUserName();
    Boolean getIsDeleted();
    Boolean getIsAdmin();

    public default Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(getIsAdmin() ? new SimpleGrantedAuthority("ROLE_ADMIN") : new SimpleGrantedAuthority("ROLE_USER"));
        return authorities;
    }
}
