package com.pragma.apiperson.domain.usecase;

import com.pragma.apiperson.domain.api.PersonServicePort;
import com.pragma.apiperson.domain.exceptions.BusinessException;
import com.pragma.apiperson.domain.exceptions.ErrorMessages;
import com.pragma.apiperson.domain.model.Person;
import com.pragma.apiperson.domain.spi.PersonPersistencePort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PersonUseCase implements PersonServicePort {

    private final PersonPersistencePort personPersistencePort;

    public PersonUseCase(PersonPersistencePort personPersistencePort) {
        this.personPersistencePort = personPersistencePort;
    }

    @Override
    public Mono<Person> save(Person person) {
        return validateFields(person)
                .flatMap(valid -> personPersistencePort.findByEmail(valid.getEmail())
                        .flatMap(exist -> Mono.<Person>error(new BusinessException(ErrorMessages.PERSON_ALREADY_EXISTS)))
                        .switchIfEmpty(Mono.defer(() -> personPersistencePort.save(valid)))
                );
    }

    @Override
    public Flux<Person> findAll() {
        return personPersistencePort.findAll();
    }

    @Override
    public Mono<Person> findById(Long id) {
        return personPersistencePort.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorMessages.PERSON_NOT_FOUND)));
    }

    private Mono<Person> validateFields(Person person) {
        String name = person.getName() == null ? null : person.getName().trim();
        String email = person.getEmail() == null ? null : person.getEmail().trim();

        if (email == null || email.isEmpty()) {
            return Mono.error(new BusinessException(ErrorMessages.PERSON_EMAIL_REQUIRED));
        }
        if (name == null || name.isEmpty()) {
            return Mono.error(new BusinessException(ErrorMessages.PERSON_NAME_REQUIRED));
        }
        if (person.getAge() == null || person.getAge() < 0) {
            return Mono.error(new BusinessException(ErrorMessages.PERSON_AGE_INVALID));
        }

        person.setName(name);
        person.setEmail(email);

        return Mono.just(person);
    }
}
