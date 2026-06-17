package com.pragma.apiperson.infrastructure.entrypoints.handler;

import com.pragma.apiperson.domain.api.PersonServicePort;
import com.pragma.apiperson.infrastructure.entrypoints.dto.PersonDTO;
import com.pragma.apiperson.infrastructure.entrypoints.mapper.PersonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PersonHandler {

    private final PersonMapper personMapper;
    private final PersonServicePort personServicePort;

    public Mono<ServerResponse> save(ServerRequest request) {
        return request.bodyToMono(PersonDTO.class)
                .map(personMapper::toDomain)
                .flatMap(personServicePort::save)
                .map(personMapper::toResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

    public Mono<ServerResponse> getAll(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(personServicePort.findAll().map(personMapper::toResponse), PersonDTO.class);
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        return personServicePort.findById(id)
                .map(personMapper::toResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }
}
