package com.pragma.apiperson.domain.api;

import com.pragma.apiperson.domain.model.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PersonServicePort {
    Mono<Person> save(Person person);
    Flux<Person> findAll();
    Mono<Person> findById(Long id);
}
