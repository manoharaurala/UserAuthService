package org.ruby.userauthservice.dtos;

import lombok.Data;

@Data
public class SignupRequestDTO {
    private String email;
    private String name;
    private String password;
}
