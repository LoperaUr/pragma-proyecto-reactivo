package com.pragma.apiperson.domain.spi;

import com.pragma.apiperson.domain.model.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PersonPersistencePort {
    Mono<Person> findByEmail(String email);
    Mono<Person> findById(Long id);
    Flux<Person> findAll();
    Mono<Person> save(Person person);
}
