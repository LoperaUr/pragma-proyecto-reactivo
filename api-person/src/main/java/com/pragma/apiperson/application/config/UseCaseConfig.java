package com.pragma.apiperson.application.config;

import com.pragma.apiperson.domain.spi.PersonPersistencePort;
import com.pragma.apiperson.domain.usecase.PersonUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UseCaseConfig {

    private final PersonPersistencePort personPersistencePort;

    @Bean
    public PersonUseCase personUseCase() {
        return new PersonUseCase(personPersistencePort);
    }


}
