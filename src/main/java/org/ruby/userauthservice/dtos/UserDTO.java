package org.ruby.userauthservice.dtos;

import lombok.Data;

import java.util.List;

@Data
public class UserDTO {
    private String email;
    private String name;
    private List<RoleDTO> roles;
}
