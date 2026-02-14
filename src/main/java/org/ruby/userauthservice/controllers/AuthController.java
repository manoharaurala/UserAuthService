package org.ruby.userauthservice.controllers;

import org.ruby.userauthservice.dtos.*;
import org.ruby.userauthservice.models.User;
import org.ruby.userauthservice.pojos.UserToken;
import org.ruby.userauthservice.services.IAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
        User createdUser = authService.signup(signupRequestDTO.getEmail(),
                signupRequestDTO.getName(),
                signupRequestDTO.getPassword());
        if (createdUser == null) throw new RuntimeException("Error while creating user");
        return new ResponseEntity<>(UserMapper.mapToDTO(createdUser), HttpStatus.CREATED);

    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
         /*
            How do we return the token in response?
            We will add token into headers and we can easily set headers in ResponseEntity

            MultiValueMap is used for representing headers and the key names here
            should be key against which we will add this token?

            The token i want to send back to client should in form of what in frontend??
            ---Cookies
             */

        UserToken loggedInUser = authService.login(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        /*
            cookie, token
            cookie, session id
            cookie, something else...
        */
        headers.add("Authorization Bearer", loggedInUser.getToken());
        headers.add(HttpHeaders.COOKIE, loggedInUser.getToken());
        HttpHeaders responseHeaders = new HttpHeaders(headers);

        return new ResponseEntity<>(
                UserMapper.mapToDTO(loggedInUser.getUser()),
                responseHeaders,
                HttpStatus.OK
        );

    }

    /*
        Input : Token
        Output: boolean
        Type: POST because we will send token in request body

         */

    @PostMapping("/validateToken")
    public ResponseEntity<String> validateToken(@RequestBody ValidateTokenDTO validateTokenDTO) {
        Boolean result = authService.validateToken(validateTokenDTO.getToken());

        if (result == false) {
            throw new RuntimeException("Invalid token");
        } else {
            return new ResponseEntity<>("Token is valid", HttpStatus.OK);
        }
    }


}
