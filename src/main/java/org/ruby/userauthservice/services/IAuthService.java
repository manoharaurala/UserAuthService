package org.ruby.userauthservice.services;

import org.ruby.userauthservice.models.User;

public interface IAuthService {
    User signup(String email, String name, String password);

    User login(String email, String password);

}
