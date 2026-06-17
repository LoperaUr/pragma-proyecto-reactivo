package com.pragma.apiperson.infrastructure.adapters.persistence.mapper;

import com.pragma.apiperson.domain.model.Person;
import com.pragma.apiperson.infrastructure.adapters.persistence.entity.PersonEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersonEntityMapper {
    Person toDomain(PersonEntity person);

    PersonEntity toEntity(Person person);
}
