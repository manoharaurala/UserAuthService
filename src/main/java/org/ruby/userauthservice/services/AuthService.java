package org.ruby.userauthservice.services;

import org.ruby.userauthservice.exceptions.IncorrectPasswordException;
import org.ruby.userauthservice.exceptions.UserAlreadyExistException;
import org.ruby.userauthservice.exceptions.UserNotRegisteredException;
import org.ruby.userauthservice.models.Role;
import org.ruby.userauthservice.models.State;
import org.ruby.userauthservice.models.User;
import org.ruby.userauthservice.repositories.RoleRepo;
import org.ruby.userauthservice.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService implements IAuthService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    /*
    encode(password) before saving to DB 128bit random salt and cost factor of 10 by default
    salt-> unique random value added to password before hashing to prevent rainbow table attacks
    cost factor-> number of iterations to increase hashing time and make brute-force attacks more difficult

    matches(rawPassword, encodedPassword) to verify password during login
    rawPassword-> password entered by user during login
    encodedPassword-> hashed password stored in DB
    1.Extract salt and cost factor from encodedPassword
    2.Hash rawPassword using extracted salt and cost factor
    3.Compare hashed rawPassword with encodedPassword, if they match return true else false

     */


    public AuthService(UserRepo userRepo, RoleRepo roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }


    @Override
    public User signup(String email, String name, String password) {
        Optional<User> existingUser = userRepo.findByEmail(email);
        if (existingUser.isPresent())
            throw new UserAlreadyExistException("User with email " + email + " already exists");
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setState(State.ACTIVE);
        /*
        what else to set?
        Be default, user role is DEFAULT
         */
        Role role;
        Optional<Role> optionalRole = roleRepo.findByName("DEFAULT");
        if (optionalRole.isPresent()) {
            role = optionalRole.get();
        } else {
            role = new Role();
            role.setName("DEFAULT");
            roleRepo.save(role);
        }
        List<Role> roles = new ArrayList<>();
        roles.add(role);
        user.setRoles(roles);
        return userRepo.save(user);
    }

    @Override
    public User login(String email, String password) {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if (optionalUser.isEmpty())
            throw new UserNotRegisteredException("User with email " + email + " is not registered");
        User user = optionalUser.get();
        if (passwordEncoder.matches(password, user.getPassword())) return user;
        throw new IncorrectPasswordException("Incorrect password for user with email " + email);
    }
}

/*
Store password in DB in hashed format using bcrypt or argon2
Encode Password using BCryptPasswordEncoder or Argon2PasswordEncoder before saving to DB
Use BCryptPasswordEncoder or Argon2PasswordEncoder to verify password during login
 */
