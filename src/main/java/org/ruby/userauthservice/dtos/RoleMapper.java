package org.ruby.userauthservice.dtos;

import org.ruby.userauthservice.models.Role;

public class RoleMapper {
    public static RoleDTO toDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setRoleName(role.getName());
        return dto;
    }

    public static Role fromDTO(RoleDTO dto) {
        Role role = new Role();
        role.setName(dto.getRoleName());
        return role;
    }

}
