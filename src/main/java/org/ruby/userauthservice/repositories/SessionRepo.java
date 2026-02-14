package org.ruby.userauthservice.repositories;

import org.ruby.userauthservice.models.Session;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SessionRepo extends CrudRepository<Session, Long> {
    Optional<Session> findByToken(String token);
}
