# Diagramas de Arquitectura

## Arquitectura de Microservicios (C4 - Contenedores)

```mermaid
graph TB
    Admin[👤 Administrador]
    Browser[🌐 Navegador<br/>JavaScript, Angular/React]

    Admin --> Browser

    subgraph Sistema Bootcamp
        MS_Persona[Microservicio Persona<br/>Webflux, Swagger]
        MS_Bootcamp[Microservicio Bootcamp<br/>Webflux, Swagger]
        MS_Capacidad[Microservicio Capacidad<br/>Webflux, Swagger]
        MS_Tecnologia[Microservicio Tecnología<br/>Webflux, SpringSecurity, Swagger]
        MS_Reporte[Microservicio Reporte<br/>Webflux, SpringSecurity, Swagger]

        DB_Persona[(BD Personas<br/>MySQL)]
        DB_Bootcamp[(BD Bootcamp<br/>MySQL)]
        DB_Capacidad[(BD Capacidad<br/>MySQL)]
        DB_Tecnologia[(BD Tecnología<br/>MySQL)]
        DB_Reporte[(BD Reporte<br/>MongoDB)]
    end

    Browser -->|JSON/HTTP| MS_Tecnologia
    Browser -->|JSON/HTTP| MS_Capacidad
    Browser -->|JSON/HTTP| MS_Reporte

    MS_Capacidad -->|JSON/HTTP| MS_Tecnologia
    MS_Capacidad -->|JSON/HTTP| MS_Bootcamp

    MS_Persona -->|TCP/IP| DB_Persona
    MS_Bootcamp -->|TCP/IP| DB_Bootcamp
    MS_Capacidad -->|TCP/IP| DB_Capacidad
    MS_Tecnologia -->|TCP/IP| DB_Tecnologia
    MS_Reporte -->|TCP/IP| DB_Reporte
```

---

## Base de Datos Relacional - Tecnología

```mermaid
erDiagram
    TECNOLOGIAS {
        int id PK
        varchar nombre
        varchar descripcion
    }

    CAPACIDAD_TECNOLOGIAS {
        int id PK
        int id_tecnologia FK
        int id_capacidad FK
    }

    TECNOLOGIAS ||--o{ CAPACIDAD_TECNOLOGIAS : "tiene"
```

## Base de Datos Relacional - Capacidad

```mermaid
erDiagram
    CAPACIDADES {
        int id PK
        varchar nombre
        varchar descripcion
    }

    CAPACIDAD_TECNOLOGIAS {
        int id PK
        int id_tecnologia FK
        int id_capacidad FK
    }

    CAPACIDAD_BOOTCAMP {
        int id PK
        int id_capacidad FK
        int id_bootcamp FK
    }

    CAPACIDADES ||--o{ CAPACIDAD_TECNOLOGIAS : "tiene"
    CAPACIDADES ||--o{ CAPACIDAD_BOOTCAMP : "pertenece"
```

## Base de Datos Relacional - Bootcamp

```mermaid
erDiagram
    BOOTCAMP {
        int id PK
        varchar nombre
        varchar descripcion
    }

    CAPACIDAD_BOOTCAMP {
        int id PK
        int id_capacidad FK
        int id_bootcamp FK
    }

    BOOTCAMP_PERSONA {
        int id PK
        int id_persona FK
        int id_bootcamp FK
    }

    BOOTCAMP ||--o{ CAPACIDAD_BOOTCAMP : "tiene"
    BOOTCAMP ||--o{ BOOTCAMP_PERSONA : "inscribe"
```

## Base de Datos Relacional - Persona

```mermaid
erDiagram
    PERSONA {
        int id PK
        varchar nombre
        varchar correo
        int edad
    }

    BOOTCAMP_PERSONA {
        int id PK
        int id_persona FK
        int id_bootcamp FK
    }

    PERSONA ||--o{ BOOTCAMP_PERSONA : "se inscribe"
```

## Base de Datos No Relacional - Reporte (MongoDB)

```mermaid
erDiagram
    REPORTE_BOOTCAMP {
        ObjectId _id PK
        string nombre_bootcamp
        string descripcion
        date fecha_lanzamiento
        int duracion
        int cantidad_capacidades
        int cantidad_tecnologias
        int cantidad_personas_inscritas
    }
```

> **Nota:** La estructura de la BD de reporte es una propuesta abierta. Cada implementación puede definir las colecciones según las métricas que necesite.

---

## Modelo Completo de Relaciones

```mermaid
erDiagram
    TECNOLOGIAS ||--o{ CAPACIDAD_TECNOLOGIAS : "1:N"
    CAPACIDADES ||--o{ CAPACIDAD_TECNOLOGIAS : "1:N"
    CAPACIDADES ||--o{ CAPACIDAD_BOOTCAMP : "1:N"
    BOOTCAMP ||--o{ CAPACIDAD_BOOTCAMP : "1:N"
    BOOTCAMP ||--o{ BOOTCAMP_PERSONA : "1:N"
    PERSONA ||--o{ BOOTCAMP_PERSONA : "1:N"

    TECNOLOGIAS {
        int id PK
        varchar nombre
        varchar descripcion
    }
    CAPACIDADES {
        int id PK
        varchar nombre
        varchar descripcion
    }
    CAPACIDAD_TECNOLOGIAS {
        int id PK
        int id_tecnologia FK
        int id_capacidad FK
    }
    BOOTCAMP {
        int id PK
        varchar nombre
        varchar descripcion
    }
    CAPACIDAD_BOOTCAMP {
        int id PK
        int id_capacidad FK
        int id_bootcamp FK
    }
    PERSONA {
        int id PK
        varchar nombre
        varchar correo
        int edad
    }
    BOOTCAMP_PERSONA {
        int id PK
        int id_persona FK
        int id_bootcamp FK
    }
```
