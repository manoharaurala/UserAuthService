package org.ruby.userauthservice.services;

import org.ruby.userauthservice.models.User;
import org.ruby.userauthservice.pojos.UserToken;

public interface IAuthService {
    User signup(String email, String name, String password);

    UserToken login(String email, String password);

    Boolean validateToken(String token);

}
