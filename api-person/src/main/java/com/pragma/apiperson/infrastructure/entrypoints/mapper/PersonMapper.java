package com.pragma.apiperson.infrastructure.entrypoints.mapper;

import com.pragma.apiperson.domain.model.Person;
import com.pragma.apiperson.infrastructure.entrypoints.dto.PersonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersonMapper {
    Person toDomain(PersonDTO personDTO);

    PersonDTO toResponse(Person person);
}
