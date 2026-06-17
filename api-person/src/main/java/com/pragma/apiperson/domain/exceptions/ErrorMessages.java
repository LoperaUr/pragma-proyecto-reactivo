package com.pragma.apiperson.domain.exceptions;

public final class ErrorMessages {

    private ErrorMessages() {}

    public static final String PERSON_NAME_REQUIRED = "Person name is required";
    public static final String PERSON_EMAIL_REQUIRED = "Person email is required";
    public static final String PERSON_AGE_INVALID = "Person age must be a positive number";
    public static final String PERSON_ALREADY_EXISTS = "A person with this email already exists";
    public static final String PERSON_NOT_FOUND = "Person not found";
}
