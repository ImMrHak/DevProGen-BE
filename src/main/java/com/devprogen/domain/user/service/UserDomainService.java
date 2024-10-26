package com.devprogen.domain.user.service;

import com.devprogen.domain.user.model.User;
import com.devprogen.domain.user.projection.UserSignInProjection;
import com.devprogen.infrastructure.persistence.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserDomainService {
    private final JpaUserRepository jpaUserRepository;

    // Fetch all attributes
    public List<User> findAll() {
        return jpaUserRepository.findAll();
    }

    // Fetch by ID
    public User findById(Long id) {
        return jpaUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attribute not found"));
    }

    // Save attribute
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    // Delete attribute
    public void delete(Long id) {
        jpaUserRepository.deleteById(id);
    }

    // Find By Username
    public User findByUserName(String username) {
        return jpaUserRepository.findByUserName(username);
    }

    public User loadUserByUsername(String username) {
        return jpaUserRepository.findByUserName(username);
    }

    public UserSignInProjection findProjectedUserByUserName(String username){
        return jpaUserRepository.findProjectedUserByUserName(username);
    }

    public UserSignInProjection findProjectedUserByEmail(String email){
        return jpaUserRepository.findProjectedUserByEmail(email);
    }

    public Boolean existsByUserNameOrEmail(String userName, String email){
        return jpaUserRepository.existsByUserNameOrEmail(userName, email);
    }
}
