package org.example.carrentapp.service;

import org.example.carrentapp.entity.Role;
import org.example.carrentapp.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository repo;

    public RoleService(RoleRepository repo) {
        this.repo = repo;
    }

    public List<Role> getAllRoles() {
        return repo.findAll();
    }

    public Role createRole(Role role) {
        // ignoruj ewentualne id z payloadu, zawsze twórz nową encję
        role.setId(null);
        return repo.save(role);
    }

    public Role getRoleById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Role updateRole(Long id, Role payload) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setName(payload.getName());
                    return repo.save(existing);
                })
                .orElse(null);
    }

    public boolean deleteRole(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}