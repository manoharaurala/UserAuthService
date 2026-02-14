package org.ruby.userauthservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Role extends BaseModel {
    private String name;//Mentor, Instructor, Admin
    /*
    allowed permissions for this role
     */
    @ManyToMany(mappedBy = "roles")
    private List<User> users;

}
