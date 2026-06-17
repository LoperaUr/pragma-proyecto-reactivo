package com.pragma.apiperson.domain.usecase;

import com.pragma.apiperson.domain.exceptions.BusinessException;
import com.pragma.apiperson.domain.exceptions.ErrorMessages;
import com.pragma.apiperson.domain.model.Person;
import com.pragma.apiperson.domain.spi.PersonPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonUseCaseTest {

    @Mock
    private PersonPersistencePort personPersistencePort;

    private PersonUseCase personUseCase;

    @BeforeEach
    void setUp() {
        personUseCase = new PersonUseCase(personPersistencePort);
    }

    private Person buildValidPerson() {
        Person person = new Person();
        person.setName("John Doe");
        person.setEmail("john@example.com");
        person.setAge(25);
        return person;
    }

    // === SAVE ===

    @Test
    void save_shouldSucceed_whenAllValidationsPass() {
        Person person = buildValidPerson();
        Person saved = buildValidPerson();
        saved.setId(1L);

        when(personPersistencePort.findByEmail("john@example.com")).thenReturn(Mono.empty());
        when(personPersistencePort.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(personUseCase.save(person))
                .expectNextMatches(p -> p.getId().equals(1L) && p.getEmail().equals("john@example.com"))
                .verifyComplete();
    }

    @Test
    void save_shouldFail_whenEmailIsNull() {
        Person person = buildValidPerson();
        person.setEmail(null);

        StepVerifier.create(personUseCase.save(person))
                .expectErrorMatches(e -> e instanceof BusinessException
                        && e.getMessage().equals(ErrorMessages.PERSON_EMAIL_REQUIRED))
                .verify();
    }

    @Test
    void save_shouldFail_whenEmailIsEmpty() {
        Person person = buildValidPerson();
        person.setEmail("   ");

        StepVerifier.create(personUseCase.save(person))
                .expectErrorMatches(e -> e instanceof BusinessException
                        && e.getMessage().equals(ErrorMessages.PERSON_EMAIL_REQUIRED))
                .verify();
    }

    @Test
    void save_shouldFail_whenNameIsNull() {
        Person person = buildValidPerson();
        person.setName(null);

        StepVerifier.create(personUseCase.save(person))
                .expectErrorMatches(e -> e instanceof BusinessException
                        && e.getMessage().equals(ErrorMessages.PERSON_NAME_REQUIRED))
                .verify();
    }

    @Test
    void save_shouldFail_whenNameIsEmpty() {
        Person person = buildValidPerson();
        person.setName("   ");

        StepVerifier.create(personUseCase.save(person))
                .expectErrorMatches(e -> e instanceof BusinessException
                        && e.getMessage().equals(ErrorMessages.PERSON_NAME_REQUIRED))
                .verify();
    }

    @Test
    void save_shouldFail_whenAgeIsNull() {
        Person person = buildValidPerson();
        person.setAge(null);

        StepVerifier.create(personUseCase.save(person))
                .expectErrorMatches(e -> e instanceof BusinessException
                        && e.getMessage().equals(ErrorMessages.PERSON_AGE_INVALID))
                .verify();
    }

    @Test
    void save_shouldFail_whenAgeIsNegative() {
        Person person = buildValidPerson();
        person.setAge(-1);

        StepVerifier.create(personUseCase.save(person))
                .expectErrorMatches(e -> e instanceof BusinessException
                        && e.getMessage().equals(ErrorMessages.PERSON_AGE_INVALID))
                .verify();
    }

    @Test
    void save_shouldFail_whenEmailAlreadyExists() {
        Person person = buildValidPerson();
        Person existing = buildValidPerson();
        existing.setId(2L);

        when(personPersistencePort.findByEmail("john@example.com")).thenReturn(Mono.just(existing));

        StepVerifier.create(personUseCase.save(person))
                .expectErrorMatches(e -> e instanceof BusinessException
                        && e.getMessage().equals(ErrorMessages.PERSON_ALREADY_EXISTS))
                .verify();

        verify(personPersistencePort, never()).save(any());
    }

    @Test
    void save_shouldTrimNameAndEmail() {
        Person person = buildValidPerson();
        person.setName("  John Doe  ");
        person.setEmail("  john@example.com  ");

        Person saved = new Person();
        saved.setId(1L);
        saved.setName("John Doe");
        saved.setEmail("john@example.com");
        saved.setAge(25);

        when(personPersistencePort.findByEmail("john@example.com")).thenReturn(Mono.empty());
        when(personPersistencePort.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(personUseCase.save(person))
                .expectNextMatches(p -> p.getName().equals("John Doe")
                        && p.getEmail().equals("john@example.com"))
                .verifyComplete();
    }

    // === FIND ALL ===

    @Test
    void findAll_shouldReturnAllPersons() {
        Person p1 = buildValidPerson();
        p1.setId(1L);
        Person p2 = buildValidPerson();
        p2.setId(2L);
        p2.setEmail("jane@example.com");

        when(personPersistencePort.findAll()).thenReturn(Flux.just(p1, p2));

        StepVerifier.create(personUseCase.findAll())
                .expectNextMatches(p -> p.getId().equals(1L))
                .expectNextMatches(p -> p.getId().equals(2L))
                .verifyComplete();
    }

    @Test
    void findAll_shouldReturnEmpty_whenNoPersonsExist() {
        when(personPersistencePort.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(personUseCase.findAll())
                .verifyComplete();
    }

    // === FIND BY ID ===

    @Test
    void findById_shouldReturnPerson_whenExists() {
        Person person = buildValidPerson();
        person.setId(1L);

        when(personPersistencePort.findById(1L)).thenReturn(Mono.just(person));

        StepVerifier.create(personUseCase.findById(1L))
                .expectNextMatches(p -> p.getId().equals(1L) && p.getName().equals("John Doe"))
                .verifyComplete();
    }

    @Test
    void findById_shouldFail_whenPersonNotFound() {
        when(personPersistencePort.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(personUseCase.findById(99L))
                .expectErrorMatches(e -> e instanceof BusinessException
                        && e.getMessage().equals(ErrorMessages.PERSON_NOT_FOUND))
                .verify();
    }
}
