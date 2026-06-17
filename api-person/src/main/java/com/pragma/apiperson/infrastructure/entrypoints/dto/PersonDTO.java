package com.pragma.apiperson.infrastructure.entrypoints.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PersonDTO {

    private Long id;
    private String name;
    private String email;
    private Integer age;
    private LocalDate createdAt;

}
