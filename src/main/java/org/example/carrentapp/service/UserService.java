package org.example.carrentapp.service;

import org.example.carrentapp.entity.User;
import org.example.carrentapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    public User createUser(User user) {
        // always create new entity: ignore any id from client
        user.setId(null);
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