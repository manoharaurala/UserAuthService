package org.ruby.userauthservice.pojos;

import lombok.Data;
import org.ruby.userauthservice.models.User;

@Data
public class UserToken {
    private User user;
    private String token;

    public UserToken(User user, String token) {
        this.user = user;
        this.token = token;
    }
}
