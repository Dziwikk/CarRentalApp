package org.example.carrentapp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter @Setter @NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private String email;

    public void setRoles(Set<String> name) {
    }
}

