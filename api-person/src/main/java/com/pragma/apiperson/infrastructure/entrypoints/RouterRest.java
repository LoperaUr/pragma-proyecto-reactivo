package com.pragma.apiperson.infrastructure.entrypoints;

import com.pragma.apiperson.infrastructure.entrypoints.handler.PersonHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterRest {

    @Bean
    public RouterFunction<ServerResponse> personRouter(PersonHandler handler) {
        return RouterFunctions.route()
                .path("/api/persons", builder -> builder
                        .POST("", handler::save)
                        .GET("", handler::getAll)
                        .GET("/{id}", handler::getById)
                )
                .build();
    }
}
