package org.ruby.userauthservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Session extends BaseModel {
    private String token;

    @ManyToOne
    private User user;

    /*
    User -> Session
    1:n

    Session->User
    1:1

    User->Session
    1:n
     */

}
