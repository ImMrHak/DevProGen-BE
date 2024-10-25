package com.devprogen.infrastructure.persistence;

import com.devprogen.domain.user.model.User;
import com.devprogen.domain.user.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {
}