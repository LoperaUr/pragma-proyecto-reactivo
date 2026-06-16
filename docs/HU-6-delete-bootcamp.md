# HU-6: Eliminar Bootcamp — Diagramas de Flujo

## Contexto

La eliminación es **distribuida** (datos en 3 bases de datos, comunicadas por HTTP):

- `api-bootcamp` → tabla `bootcamp`
- `api-capability` → tablas `capability`, `bootcamp_capability`
- `api-technology` → tablas `technology`, `capability_technology`

R2DBC soporta transacciones locales por servicio, pero **NO transacciones distribuidas** entre microservicios.

---

## Alternativa A: SAGA Orquestada con Compensación

El `api-bootcamp` orquesta todo. Si un paso falla, ejecuta compensaciones en orden inverso.

```mermaid
sequenceDiagram
    participant Client
    participant Bootcamp as api-bootcamp :8082
    participant Capability as api-capability :8081
    participant Technology as api-technology :8080

    Client->>Bootcamp: DELETE /api/bootcamps/{id}

    Note over Bootcamp: FASE 1: RECOLECCIÓN DE DATOS

    Bootcamp->>Bootcamp: Buscar bootcamp por ID (local DB)
    Bootcamp->>Capability: GET /api/bootcamps/{id}/capabilities
    Capability-->>Bootcamp: [capIds: 1, 3]

    loop Para cada capabilityId
        Bootcamp->>Capability: GET /api/bootcamps/capabilities/count?capId=X
        Capability-->>Bootcamp: refCount (cuántos bootcamps la referencian)
    end

    Note over Bootcamp: Filtrar: capsToDelete = refCount == 1<br/>capsToKeep = refCount > 1

    loop Para cada capToDelete
        Bootcamp->>Technology: GET /api/capabilities/{capId}/technologies
        Technology-->>Bootcamp: [techIds]
        Bootcamp->>Technology: GET /api/capabilities/technologies/count?techId=X
        Technology-->>Bootcamp: refCount por tecnología
    end

    Note over Bootcamp: Filtrar: techsToDelete = refCount == 1

    Note over Bootcamp,Technology: FASE 2: ELIMINACIÓN

    Bootcamp->>Capability: DELETE /api/bootcamps/{id}/capabilities (relaciones)
    Capability-->>Bootcamp: OK

    loop Para cada capToDelete
        Bootcamp->>Technology: DELETE /api/capabilities/{capId}/technologies (relaciones)
        Technology-->>Bootcamp: OK
    end

    loop Para cada techToDelete
        Bootcamp->>Technology: DELETE /api/technologies/{techId}
        Technology-->>Bootcamp: OK
    end

    loop Para cada capToDelete
        Bootcamp->>Capability: DELETE /api/capabilities/{capId}
        Capability-->>Bootcamp: OK
    end

    Bootcamp->>Bootcamp: DELETE bootcamp (local DB)
    Bootcamp-->>Client: 204 No Content
```

### Flujo de Compensación (si falla en Fase 2)

```mermaid
sequenceDiagram
    participant Bootcamp as api-bootcamp
    participant Capability as api-capability
    participant Technology as api-technology

    Note over Bootcamp,Technology: FALLO en paso N → COMPENSAR en orden inverso

    alt Falla DELETE capability
        Bootcamp->>Technology: POST /api/capabilities/technologies (re-crear relaciones)
        Bootcamp->>Capability: POST /api/bootcamps/capabilities (re-crear relaciones)
    end

    alt Falla DELETE technology
        Bootcamp->>Technology: POST /api/technologies (re-crear tecnología)
        Bootcamp->>Technology: POST /api/capabilities/technologies (re-crear relación)
    end

    Bootcamp->>Bootcamp: NO eliminar bootcamp
    Note over Bootcamp: Retornar 500 Internal Server Error
```

### Pros y Contras

| Pros | Contras |
|------|---------|
| Control total del orquestador | Muchas llamadas HTTP (latencia) |
| Fácil de entender | Compensaciones pueden fallar (doble fallo) |
| Compatible con WebClient reactivo | No es atómico en sentido estricto |

---

## Alternativa B: Two-Phase (Soft Delete + Confirmación)

Se marca todo como "pendiente de eliminación" y luego se confirma. Si falla, se revierte la marca.

```mermaid
sequenceDiagram
    participant Client
    participant Bootcamp as api-bootcamp :8082
    participant Capability as api-capability :8081
    participant Technology as api-technology :8080

    Client->>Bootcamp: DELETE /api/bootcamps/{id}

    Note over Bootcamp,Technology: FASE 1: PREPARAR (marcar status = DELETING)

    Bootcamp->>Bootcamp: UPDATE bootcamp SET status = 'DELETING'

    Bootcamp->>Capability: POST /api/capabilities/prepare-delete<br/>{bootcampId, capIds}
    Note over Capability: Verificar refCount de cada cap<br/>Marcar caps con refCount=1 como DELETING

    Capability->>Technology: POST /api/technologies/prepare-delete<br/>{capId, techIds}
    Note over Technology: Verificar refCount de cada tech<br/>Marcar techs únicas como DELETING
    Technology-->>Capability: PREPARED
    Capability-->>Bootcamp: PREPARED

    Note over Bootcamp,Technology: FASE 2: CONFIRMAR (eliminar registros marcados)

    Bootcamp->>Capability: POST /api/capabilities/confirm-delete
    Capability->>Technology: POST /api/technologies/confirm-delete
    Note over Technology: DELETE WHERE status = 'DELETING'
    Technology-->>Capability: OK
    Note over Capability: DELETE WHERE status = 'DELETING'
    Capability-->>Bootcamp: OK
    Bootcamp->>Bootcamp: DELETE bootcamp

    Bootcamp-->>Client: 204 No Content
```

### Flujo de Rollback

```mermaid
sequenceDiagram
    participant Bootcamp as api-bootcamp
    participant Capability as api-capability
    participant Technology as api-technology

    Note over Bootcamp,Technology: FALLA EN FASE 2 → ROLLBACK

    Bootcamp->>Capability: POST /api/capabilities/rollback-delete
    Capability->>Technology: POST /api/technologies/rollback-delete
    Note over Technology: UPDATE SET status = 'ACTIVE'<br/>WHERE status = 'DELETING'
    Technology-->>Capability: OK
    Note over Capability: UPDATE SET status = 'ACTIVE'<br/>WHERE status = 'DELETING'
    Capability-->>Bootcamp: OK

    Bootcamp->>Bootcamp: UPDATE bootcamp SET status = 'ACTIVE'
    Note over Bootcamp: Retornar 500
```

### Pros y Contras

| Pros | Contras |
|------|---------|
| Más seguro: datos no se pierden hasta confirmación | Requiere agregar columna `status` a todas las tablas |
| Job de limpieza puede resolver estados huérfanos | Mayor complejidad (3 fases) |
| Recuperable ante caídas del proceso | Necesita scheduled job para DELETING huérfanos |

---

## Alternativa C: Orden Bottom-Up (Best Effort) ⭐ RECOMENDADA

Cada microservicio es responsable de eliminar sus datos huérfanos. El orden bottom-up garantiza que si falla un nivel inferior, el superior no se elimina.

```mermaid
sequenceDiagram
    participant Client
    participant Bootcamp as api-bootcamp :8082
    participant Capability as api-capability :8081
    participant Technology as api-technology :8080

    Client->>Bootcamp: DELETE /api/bootcamps/{id}
    Bootcamp->>Bootcamp: findById(id) → bootcamp

    alt bootcamp no existe
        Bootcamp-->>Client: 404 Not Found
    end

    Note over Bootcamp,Technology: PASO 1: Delegar eliminación en cascada a api-capability

    Bootcamp->>Capability: DELETE /api/bootcamps/{bootcampId}/full-delete
    Note over Capability: Obtener capIds del bootcamp<br/>Eliminar relaciones bootcamp_capability

    loop Para cada capability huérfana (refCount == 0)
        Note over Capability: PASO 2: Delegar a api-technology

        Capability->>Technology: DELETE /api/capabilities/{capId}/full-delete

        Note over Technology: Obtener techIds de la capability<br/>Eliminar relaciones capability_technology<br/>(transacción local R2DBC)

        loop Para cada technology huérfana (refCount == 0)
            Note over Technology: DELETE technology
        end

        Technology-->>Capability: 204 OK
        Note over Capability: DELETE capability<br/>(transacción local R2DBC)
    end

    Capability-->>Bootcamp: 204 OK

    Note over Bootcamp: PASO 3: Eliminar bootcamp local (transacción R2DBC)
    Bootcamp->>Bootcamp: DELETE bootcamp
    Bootcamp-->>Client: 204 No Content
```

### Detalle: Lógica interna de cada servicio

```mermaid
flowchart TD
    subgraph "api-bootcamp (orquestador)"
        A[DELETE /api/bootcamps/id] --> B{¿Existe bootcamp?}
        B -->|No| C[404 Not Found]
        B -->|Sí| D[Llamar api-capability:<br/>DELETE /bootcamps/id/full-delete]
        D --> E{¿Respuesta OK?}
        E -->|Error| F[500 - No eliminar bootcamp<br/>Propagar error]
        E -->|OK| G[DELETE bootcamp local<br/>Transacción R2DBC]
        G --> H[204 No Content]
    end

    subgraph "api-capability"
        I[DELETE /bootcamps/bootcampId/full-delete] --> J[Obtener capIds del bootcamp]
        J --> K[DELETE bootcamp_capability<br/>WHERE bootcampId = X]
        K --> L{Para cada cap:<br/>¿refCount en bootcamp_capability == 0?}
        L -->|Sí: huérfana| M[Llamar api-technology:<br/>DELETE /capabilities/capId/full-delete]
        M --> N{¿OK?}
        N -->|OK| O[DELETE capability]
        N -->|Error| P[onErrorResume: propagar error]
        L -->|No: referenciada| Q[No eliminar cap]
        O --> R[Continuar con siguiente cap]
        Q --> R
    end

    subgraph "api-technology"
        S[DELETE /capabilities/capId/full-delete] --> T[Obtener techIds de la capability]
        T --> U[DELETE capability_technology<br/>WHERE capabilityId = X]
        U --> V{Para cada tech:<br/>¿refCount en capability_technology == 0?}
        V -->|Sí: huérfana| W[DELETE technology]
        V -->|No: referenciada| X[No eliminar tech]
        W --> Y[Continuar]
        X --> Y
    end
```

### Manejo de Errores

```mermaid
flowchart TD
    A[Falla en api-technology] --> B[api-capability recibe error]
    B --> C[api-capability NO elimina la capability]
    C --> D[Propaga error a api-bootcamp]
    D --> E[api-bootcamp NO elimina el bootcamp]
    E --> F[Retorna 500 al cliente]

    G[Falla en api-capability] --> H[api-bootcamp recibe error]
    H --> I[api-bootcamp NO elimina el bootcamp]
    I --> J[Retorna 500 al cliente]

    K[Caso edge: api-technology eliminó techs<br/>pero falla al responder] --> L[Tecnologías quedan huérfanas<br/>sin relación capability_technology]
    L --> M[Solución: Job de reconciliación<br/>o endpoint de limpieza]
```

### Pros y Contras

| Pros | Contras |
|------|---------|
| Simple, sigue el patrón actual del proyecto | Caso edge: techs huérfanas si falla parcialmente |
| Orden bottom-up minimiza inconsistencias | No es 100% ACID distribuido |
| Cada servicio maneja transacción local R2DBC | — |
| Bootcamp solo se elimina si todo salió bien | — |
| Pocos endpoints nuevos | — |

---

## Alternativa D: Bottom-Up + Compensación ⭐ RECOMENDADA

Combina la simplicidad del flujo bottom-up (C) con rollback real mediante compensación. En el caso feliz es idéntico a C. La diferencia: si un paso falla después de que un servicio inferior ya hizo commit, se restaura lo eliminado.

### Flujo Principal

```mermaid
sequenceDiagram
    participant Client
    participant Bootcamp as api-bootcamp :8082
    participant Capability as api-capability :8081
    participant Technology as api-technology :8080

    Client->>Bootcamp: DELETE /api/bootcamps/{id}
    Bootcamp->>Bootcamp: findById(id)

    alt bootcamp no existe
        Bootcamp-->>Client: 404 Not Found
    end

    Bootcamp->>Capability: DELETE /api/bootcamps/{id}/full-delete

    Note over Capability: 1. Obtener capIds del bootcamp<br/>2. Eliminar relaciones bootcamp_capability<br/>3. Identificar caps huérfanas (refCount == 0)

    loop Cada capability huérfana
        Capability->>Technology: DELETE /api/capabilities/{capId}/full-delete
        Note over Technology: Transacción local R2DBC:<br/>- Obtener techIds de la capability<br/>- DELETE capability_technology<br/>- Identificar techs huérfanas (refCount == 0)<br/>- DELETE technologies huérfanas<br/>- Retornar snapshot de lo eliminado
        Technology-->>Capability: 200 {deletedTechIds, deletedRelations}
    end

    Note over Capability: Transacción local R2DBC:<br/>DELETE capabilities huérfanas
    Capability-->>Bootcamp: 200 OK

    Bootcamp->>Bootcamp: DELETE bootcamp (transacción local)
    Bootcamp-->>Client: 204 No Content
```

### Flujo de Compensación (fallo después de commit parcial)

```mermaid
sequenceDiagram
    participant Bootcamp as api-bootcamp :8082
    participant Capability as api-capability :8081
    participant Technology as api-technology :8080

    Note over Capability: api-technology YA eliminó techs de cap 3<br/>Falla al intentar DELETE capability 3

    Note over Capability,Technology: COMPENSACIÓN

    Capability->>Technology: POST /api/capabilities/{capId}/restore<br/>{deletedTechIds: [11,12], deletedRelations: [...]}
    Note over Technology: Re-insertar technologies<br/>Re-insertar capability_technology
    Technology-->>Capability: 200 Restored

    Note over Capability: Re-insertar bootcamp_capability

    Capability-->>Bootcamp: 500 Error (propagado)
    Bootcamp-->>Client: 500 Error<br/>(bootcamp NO se elimina)
```

### Lógica Interna Detallada

```mermaid
flowchart TD
    subgraph "api-bootcamp"
        A[DELETE /api/bootcamps/id] --> B{¿Existe?}
        B -->|No| C[404]
        B -->|Sí| D[DELETE /bootcamps/id/full-delete<br/>en api-capability]
        D --> E{¿OK?}
        E -->|Error| F[500 - Bootcamp NO se elimina]
        E -->|OK| G[DELETE bootcamp local]
        G --> H[204 No Content]
    end

    subgraph "api-capability"
        I[DELETE /bootcamps/id/full-delete] --> J[Obtener capIds]
        J --> K[DELETE bootcamp_capability]
        K --> L[Identificar caps huérfanas<br/>refCount == 0]
        L --> M[Para cada huérfana:<br/>DELETE /capabilities/capId/full-delete<br/>en api-technology]
        M --> N[Guardar snapshot de cada respuesta]
        N --> O[DELETE capabilities huérfanas]
        O --> P{¿OK?}
        P -->|Sí| Q[200 OK]
        P -->|No| R[COMPENSAR:<br/>POST /restore a api-technology<br/>RE-INSERTAR bootcamp_capability]
        R --> S[Propagar 500]
    end

    subgraph "api-technology"
        T[DELETE /capabilities/capId/full-delete] --> U[Obtener techIds]
        U --> V[DELETE capability_technology]
        V --> W[Identificar techs huérfanas<br/>refCount == 0]
        W --> X[DELETE technologies huérfanas]
        X --> Y[Retornar snapshot:<br/>deletedTechIds + deletedRelations]
    end

    subgraph "api-technology (restore)"
        Z[POST /capabilities/capId/restore] --> AA[RE-INSERT technologies]
        AA --> AB[RE-INSERT capability_technology]
        AB --> AC[200 Restored]
    end
```

### Estructura del Snapshot (lo que retorna cada full-delete)

```mermaid
classDiagram
    class DeleteSnapshot {
        +Long capabilityId
        +List~TechSnapshot~ deletedTechnologies
        +List~RelationSnapshot~ deletedRelations
    }
    class TechSnapshot {
        +Long id
        +String name
        +String description
    }
    class RelationSnapshot {
        +Long capabilityId
        +Long technologyId
    }
    DeleteSnapshot --> TechSnapshot
    DeleteSnapshot --> RelationSnapshot
```

### Escenarios de Fallo y Comportamiento

```mermaid
flowchart TD
    A[Falla api-technology en full-delete] --> B[api-capability recibe error]
    B --> C[Compensar caps anteriores<br/>que sí se eliminaron]
    C --> D[Re-insertar bootcamp_capability]
    D --> E[Propagar error → bootcamp NO se elimina]

    F[Falla api-capability en DELETE capability<br/>después de que technology commitió] --> G[Compensar: POST /restore]
    G --> H[Re-insertar bootcamp_capability]
    H --> I[Propagar error → bootcamp NO se elimina]

    J[Falla la compensación misma<br/>doble fallo - extremadamente raro] --> K[Log de error + alerta]
    K --> L[Job de reconciliación periódico<br/>detecta huérfanos]
```

### Pros y Contras

| Pros | Contras |
|------|---------|
| Flujo feliz simple (bottom-up) | Complejidad media (snapshots + restore) |
| Rollback real ante fallos parciales | Doble fallo sigue siendo irrecuperable |
| No requiere cambios en schema | 2 endpoints extra (full-delete + restore) |
| Compatible con patrón actual del proyecto | — |
| Caso edge de techs huérfanas eliminado | — |
| Cada servicio maneja transacción local R2DBC | — |

---

## Comparación de Alternativas

| Criterio | A - SAGA | B - Two-Phase | C - Bottom-Up | D - Bottom-Up + Compensación ⭐ |
|----------|----------|---------------|---------------|----------------------------------|
| Complejidad | Media | Alta | Baja | Media |
| Consistencia | Alta | Muy alta | Alta (best effort) | Muy alta |
| Endpoints nuevos | ~8 | ~6 | 3 | 5 |
| Cambios en schema | No | Sí (columna status) | No | No |
| Latencia | Alta (muchas llamadas) | Media | Baja | Baja (caso feliz) |
| Rollback ante fallo parcial | Sí | Sí | No | Sí |
| Compatible con patrón actual | Sí | No | Sí | Sí |
| Caso edge huérfanos | Resuelto | Resuelto | Posible | Resuelto |

---

## Endpoints Nuevos Requeridos

| Servicio | Endpoint | Método | Propósito |
|----------|----------|--------|-----------|
| `api-bootcamp` | `/api/bootcamps/{id}` | DELETE | Orquesta eliminación |
| `api-capability` | `/api/bootcamps/{bootcampId}/full-delete` | DELETE | Elimina relaciones + caps huérfanas + compensa si falla |
| `api-capability` | `/api/bootcamps/{bootcampId}/capabilities/restore` | POST | Restaura relaciones bootcamp-capability |
| `api-technology` | `/api/capabilities/{capabilityId}/full-delete` | DELETE | Elimina relaciones + techs huérfanas, retorna snapshot |
| `api-technology` | `/api/capabilities/{capabilityId}/restore` | POST | Restaura tecnologías y relaciones desde snapshot |

---

## Recomendación Final

**Alternativa D (Bottom-Up + Compensación)** porque:

1. El caso feliz es tan simple como la C (delegación en cascada, bottom-up)
2. Agrega rollback real: si `api-technology` ya commitió y luego falla `api-capability`, se restaura todo
3. No requiere cambios en el schema de las tablas
4. Sigue el patrón del proyecto (el `save` ya usa `onErrorResume` para rollback)
5. El único caso irrecuperable es el doble fallo (falla la operación Y falla la compensación), estadísticamente despreciable
