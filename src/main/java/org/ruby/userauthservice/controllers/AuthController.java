package org.ruby.userauthservice.controllers;

import org.ruby.userauthservice.dtos.LoginRequestDTO;
import org.ruby.userauthservice.dtos.SignupRequestDTO;
import org.ruby.userauthservice.dtos.UserDTO;
import org.ruby.userauthservice.dtos.UserMapper;
import org.ruby.userauthservice.models.User;
import org.ruby.userauthservice.services.IAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }
    /*
    1. Register User

        /signup
        -Type: POST
        -Request Type:
            -name: String
            -email: String
            -password: String
         -Return Type:
               -ResponseEntity
               -body: UserDTO
                  -UserDTO: name, email, roles
               -Status: 200 if registration successful,
                        400 if user with the same email already exists

        -Post request with user details (name, email, password)
        -RequestDTO: name, email, password
        -Check if user with the same email already exists
        -Hash the password before saving to database
        -Return success message or error message if user already exists
        ResponseEntity<UserDTO> 200 OK, 400 Bad Request
        -UserDTO: name, email, roles

    2. Login User
        /login
        -Type: POST
        -Request Type:LoginRequestDTO
            -email: String
            -password: String
         -Return Type:
               -ReponseEntity<UserDTO>
                   -Header JWT token
                   -Body: UserDTO (name, email, roles)
                -Status: 200 if login successful,
                         401 if invalid credentials

     */

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody SignupRequestDTO signupRequestDTO) {
        try {
            User createdUser = authService.signup(signupRequestDTO.getEmail(),
                    signupRequestDTO.getName(),
                    signupRequestDTO.getPassword());
            if (createdUser == null) throw new RuntimeException("Error while creating user");
            return new ResponseEntity<>(UserMapper.mapToDTO(createdUser), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            User loggedInUser = authService.login(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
            if (loggedInUser == null) throw new RuntimeException("Invalid credentials");
            return new ResponseEntity<>(UserMapper.mapToDTO(loggedInUser), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

    }


}
