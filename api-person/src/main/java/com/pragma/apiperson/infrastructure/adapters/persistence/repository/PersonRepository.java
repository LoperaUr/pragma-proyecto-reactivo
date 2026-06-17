package com.pragma.apiperson.infrastructure.adapters.persistence.repository;

import com.pragma.apiperson.domain.model.Person;
import com.pragma.apiperson.infrastructure.adapters.persistence.PersonPersistenceAdapter;
import com.pragma.apiperson.infrastructure.adapters.persistence.entity.PersonEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface PersonRepository extends R2dbcRepository<PersonEntity, Long> {
    Mono<PersonEntity> findByEmail(String email);
}
