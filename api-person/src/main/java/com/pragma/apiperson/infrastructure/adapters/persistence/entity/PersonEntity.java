package com.pragma.apiperson.infrastructure.adapters.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Table("person")
@AllArgsConstructor
public class PersonEntity {

    @Id
    private Long id;
    private String name;
    private String email;
    private Integer age;
    private LocalDate createdAt;

}
