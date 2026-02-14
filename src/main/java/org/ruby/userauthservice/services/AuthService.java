package org.ruby.userauthservice.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.ruby.userauthservice.exceptions.IncorrectPasswordException;
import org.ruby.userauthservice.exceptions.UserAlreadyExistException;
import org.ruby.userauthservice.exceptions.UserNotRegisteredException;
import org.ruby.userauthservice.models.Role;
import org.ruby.userauthservice.models.Session;
import org.ruby.userauthservice.models.State;
import org.ruby.userauthservice.models.User;
import org.ruby.userauthservice.pojos.UserToken;
import org.ruby.userauthservice.repositories.RoleRepo;
import org.ruby.userauthservice.repositories.SessionRepo;
import org.ruby.userauthservice.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService implements IAuthService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final SessionRepo sessionRepo;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SecretKey secretKey;
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


    public AuthService(UserRepo userRepo, RoleRepo roleRepo, SessionRepo sessionRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.sessionRepo = sessionRepo;
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

    /*
    Map
    multiple key value pairs
    <user1, token1>
    <user2, token2>

    Pair
    <key, value>
     */


    @Override
    public UserToken login(String email, String password) {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if (optionalUser.isEmpty())
            throw new UserNotRegisteredException("User with email " + email + " is not registered");
        User user = optionalUser.get();
        if (passwordEncoder.matches(password, user.getPassword())) {
            /*
            Generate JWT and return user object with JWT
             */
            Long nowInMills = System.currentTimeMillis(); // return timestamp in epoch
            Map<String, Object> payload = Map.of(
                    "iat", nowInMills,
                    "exp", nowInMills + 10000000,
                    "userId", user.getId(),
                    "iss", "Ruby-auth-service",
                    "scope", user.getRoles().stream().map(Role::getName).toList()
            );
            /*
            Payload is generated
             */

            String jwtToken = Jwts.builder()
                    .setClaims(payload)
                    .signWith(secretKey)
                    .compact();
              /*
            Create a new logged in session for the user
             */
            Session session = new Session();
            session.setToken(jwtToken);
            session.setUser(user);
            session.setState(State.ACTIVE);
            sessionRepo.save(session);

            /*
            When you send this token to resources sever, it should
            be able to self validate the token
            I want to persist all the token that I am generating

            Ideally, for storing tokens, we should create a
            new table called as "Session"

            Diff between Sessions and cookies
            Session is used to store token in the backend
            Cookies are used to store token in the browser

            Auth service is generating so many tokens for every user
            there should be some source of truth / db where all these
            tokens should persist

             */

            /*
            We also want to return this generated token back to the client?

             */
//            System.out.println(jwtToken);
//            System.out.println(jwtToken.length());
            return new UserToken(user, jwtToken);


        }
        throw new IncorrectPasswordException("Incorrect password for user with email " + email);
    }

    @Override
    public Boolean validateToken(String token) {
        /*
        We want to check if this token is in my db or not?
        in Sessions table
         */
        Optional<Session> optionalSession = sessionRepo.findByToken(token);
        if (optionalSession.isEmpty()) return false;

        //Define Bean for SecretKey and inject here to validate token using Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
        Claims claims;
        try {
            JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
            claims = jwtParser.parseClaimsJws(token).getBody();
        } catch (Exception e) {
            Session session = optionalSession.get();
            session.setState(State.INACTIVE);
            sessionRepo.save(session);
            return false;
        }
        //System.out.println(claims);

        /*
        Extracting the payload from the JWT using a parser which contains the secret key

         */

        Long expiryTime = (Long) claims.get("exp");
        Long nowInMills = System.currentTimeMillis();

        if (nowInMills > expiryTime) {
            Session session = optionalSession.get();
            session.setState(State.INACTIVE);
            sessionRepo.save(session);
            return false;
        }

        return true;

    }
}

/*
    login should generate a JWT
    JWT - most important part of JWT?
    Payload - contains user info, client info and info about token
    Payload is also referred to as claims

    Which DS can be used to represent claims/payload?
    Map<String, Object> where key represents strings data type and
    value represents any data type
    1. createdAt (iat) = issued at
    2. expiry (exp)  = expiry
    3. userId (userId)
    4. creator (iss) iss = issued by
    5. scope (scope)

    1 s = 1000 ms
    1000 ms = 1 s
    10,000 ms = 10 s
    100,00 ms = 100 s

     */

/*
What do you need for generating signature?
(Header + payload, secret key)
Header = algorithm
secret key = need to figure out
 */

/*
Store password in DB in hashed format using bcrypt or argon2
Encode Password using BCryptPasswordEncoder or Argon2PasswordEncoder before saving to DB
Use BCryptPasswordEncoder or Argon2PasswordEncoder to verify password during login
 */

