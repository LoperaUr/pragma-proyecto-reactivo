package com.pragma.apiperson.domain.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Person {

    private Long id;
    private String name;
    private String email;
    private Integer age;
    private LocalDate createdAt;

}
