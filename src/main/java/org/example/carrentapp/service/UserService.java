package org.example.carrentapp.service;

import org.example.carrentapp.entity.Role;
import org.example.carrentapp.entity.User;
import org.example.carrentapp.repository.RoleRepository;
import org.example.carrentapp.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository repo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, RoleRepository roleRepo, PasswordEncoder encoder) {
        this.repo = repo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    public User createUser(User user) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        // Ensure unique username & email
        if (repo.findByUsername(user.getUsername()).isPresent()) {
            throw new DuplicateKeyException("Username already exists");
        }
        if (repo.existsByEmail(user.getEmail())) {
            throw new DuplicateKeyException("Email already exists");
        }
        // Reset id and encode password
        user.setId(null);
        user.setPassword(encoder.encode(user.getPassword()));

        // Resolve and set existing roles
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Set<Role> managedRoles = user.getRoles().stream()
                    .map(r -> roleRepo.findById(r.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + r.getId())))
                    .collect(Collectors.toSet());
            user.setRoles(managedRoles);
        }
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
