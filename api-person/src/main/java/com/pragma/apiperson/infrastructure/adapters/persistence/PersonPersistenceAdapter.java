package com.pragma.apiperson.infrastructure.adapters.persistence;

import com.pragma.apiperson.domain.model.Person;
import com.pragma.apiperson.domain.spi.PersonPersistencePort;
import com.pragma.apiperson.infrastructure.adapters.persistence.mapper.PersonEntityMapper;
import com.pragma.apiperson.infrastructure.adapters.persistence.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PersonPersistenceAdapter implements PersonPersistencePort {

    private final PersonRepository personRepository;
    private final PersonEntityMapper personEntityMapper;

    @Override
    public Mono<Person> findByEmail(String email) {
        return personRepository.findByEmail(email)
                .map(personEntityMapper::toDomain);
    }

    @Override
    public Mono<Person> findById(Long id) {
        return personRepository.findById(id)
                .map(personEntityMapper::toDomain);
    }

    @Override
    public Flux<Person> findAll() {
        return personRepository.findAll()
                .map(personEntityMapper::toDomain);
    }

    @Override
    public Mono<Person> save(Person person) {
        return personRepository.save(personEntityMapper.toEntity(person))
                .flatMap(saved -> personRepository.findById(saved.getId()))
                .map(personEntityMapper::toDomain);
    }
}
