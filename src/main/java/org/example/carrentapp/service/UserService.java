// src/main/java/org/example/carrentapp/service/UserService.java
package org.example.carrentapp.service;

import org.example.carrentapp.entity.User;
import org.example.carrentapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    public User createUser(User user) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        user.setId(null);
        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);
    }



    public User getUserById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public User updateUser(Long id, User payload) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setUsername(payload.getUsername());
                    existing.setEmail(payload.getEmail());
                    return repo.save(existing);
                })
                .orElse(null);
    }

    public boolean deleteUser(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}
