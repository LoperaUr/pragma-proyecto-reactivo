# Pragma Proyecto Reactivo

Proyecto que contiene los microservicios reactivos desarrollados con Spring WebFlux.

## Microservicios

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| [api-technology](./api-technology) | 8080 | Gestión de tecnologías |
| [api-capability](./api-capability) | 8081 | Gestión de capacidades |
| [api-bootcamp](./api-bootcamp) | 8082 | Gestión de bootcamps |
| [api-person](./api-person) | 8083 | Gestión de personas |
| [api-report](./api-report) | 8084 | Gestión de reportes |

## Clonar el proyecto

```bash
git clone --recurse-submodules <url-del-repo>
```

## Estructura

```
pragma-proyecto-reactivo/
├── README.md
├── api-technology/   (submodule)
├── api-capability/   (submodule)
├── api-bootcamp/     (submodule)
├── api-person/       (submodule)
└── api-report/       (submodule)
```

---

## Historias de Usuario

### HU-1: Registrar tecnologías

| Campo | Detalle |
|-------|---------|
| **Rol** | Admin |
| **Necesidad** | Registrar las tecnologías que serán usadas próximamente por las capacidades |
| **Beneficio** | Saber a qué tecnologías le está apuntando el bootcamp y agrupar de mejor manera |

**Criterios de aceptación:**
- Cada tecnología tiene 3 campos: id, nombre y descripción
- El nombre de la tecnología no se puede repetir
- Todas las tecnologías deben tener una descripción
- El tamaño máximo del nombre debe ser de 50 caracteres
- El tamaño máximo de la descripción debe ser de 90 caracteres

**Microservicio:** `api-technology`

---

### HU-2: Registrar capacidades

| Campo | Detalle |
|-------|---------|
| **Rol** | Admin |
| **Necesidad** | Registrar las capacidades |
| **Beneficio** | Agrupar tecnologías |

**Criterios de aceptación:**
- Cada capacidad tiene 3 campos: id, nombre y descripción
- Las capacidades deben tener mínimo 3 tecnologías asociadas
- Las capacidades no pueden tener tecnologías repetidas
- Una capacidad tiene máximo 20 tecnologías

**Microservicio:** `api-capability` (consume `api-technology`)

---

### HU-3: Listar capacidades

| Campo | Detalle |
|-------|---------|
| **Rol** | Admin |
| **Necesidad** | Listar las capacidades |
| **Beneficio** | Visualizar cuáles ya están creadas |

**Criterios de aceptación:**
- Se debe poder parametrizar si se quiere listar en orden descendente o ascendente por el nombre o por la cantidad de tecnologías asociadas
- El servicio debe estar paginado
- De cada capacidad listada, se debe traer el listado de tecnologías solo con nombre e id

**Microservicio:** `api-capability` (consume `api-technology`)

---

### HU-4: Registrar bootcamp

| Campo | Detalle |
|-------|---------|
| **Rol** | Admin |
| **Necesidad** | Registrar bootcamps |
| **Beneficio** | Dar inicio a eventos del bootcamp |

**Criterios de aceptación:**
- Cada bootcamp debe tener: id, nombre, descripción, fecha de lanzamiento, duración y un listado de capacidades asociadas
- Un bootcamp debe tener como mínimo 1 capacidad asociada y como máximo 4

**Microservicio:** `api-bootcamp` (consume `api-capability`)

---

### HU-5: Listar bootcamps

| Campo | Detalle |
|-------|---------|
| **Rol** | Admin |
| **Necesidad** | Listar los bootcamps |
| **Beneficio** | Visualizar cuáles ya están creados |

**Criterios de aceptación:**
- Se debe poder parametrizar si se quiere listar en orden descendente o ascendente por el nombre o por la cantidad de capacidades asociadas
- El servicio debe estar paginado
- De cada bootcamp listado, se debe traer el listado de capacidades con nombre e id, y listado de tecnologías con nombre e id

**Microservicio:** `api-bootcamp` (consume `api-capability` y `api-technology`)

---

### HU-6: Eliminar bootcamp

| Campo | Detalle |
|-------|---------|
| **Rol** | Admin |
| **Necesidad** | Eliminar los bootcamps |
| **Beneficio** | Que ya no estén disponibles para las personas |

**Criterios de aceptación:**
- Se debe eliminar tanto el bootcamp como las capacidades y tecnologías asociadas
- Las capacidades y tecnologías asociadas solo se eliminan si están referenciadas únicamente por ese bootcamp; si están referenciadas por más bootcamps, no se deben eliminar
- La operación debe ser transaccional

**Microservicio:** `api-bootcamp` (consume `api-capability` y `api-technology`)

---

### HU-7: Inscribirme en bootcamps

| Campo | Detalle |
|-------|---------|
| **Rol** | Persona |
| **Necesidad** | Inscribirme en varios bootcamps |
| **Beneficio** | Poder participar en las actividades de los bootcamps |

**Criterios de aceptación:**
- Una persona se puede inscribir en varios bootcamps a la vez, siempre y cuando no coincidan en fecha y duración
- Máximo se puede inscribir en 5 bootcamps al tiempo

**Microservicio:** `api-persona` (consume `api-bootcamp`)

---

### HU-8: Registrar reporte de bootcamp

| Campo | Detalle |
|-------|---------|
| **Rol** | Admin |
| **Necesidad** | Registrar los reportes de los bootcamps |
| **Beneficio** | Sacar métricas sobre el comportamiento de los usuarios |

**Criterios de aceptación:**
- Cada vez que se registre un bootcamp se debe guardar en la base de datos de reporte toda la información necesaria para sacar métricas
- Se debe guardar: toda la información del bootcamp, cantidad de capacidades, cantidad de tecnologías, cantidad de personas inscritas
- El guardado debe hacerse sin afectar el rendimiento del registro de bootcamp

**Microservicio:** `api-reporte` (evento desde `api-bootcamp`)

---

### HU-9: Mostrar el bootcamp con mayor cantidad de personas

| Campo | Detalle |
|-------|---------|
| **Rol** | Admin |
| **Necesidad** | Listar el bootcamp con la mayor cantidad de personas |
| **Beneficio** | Saber cuál es el bootcamp más exitoso |

**Criterios de aceptación:**
- El sistema debe retornar: toda la información del bootcamp, el nombre y correo de cada persona inscrita, cada una de las capacidades y cada una de las tecnologías asociadas al bootcamp

**Microservicio:** `api-reporte` (consume `api-bootcamp` y `api-persona`)

---

## Asignación de HUs por Microservicio

| HU | Funcionalidad | Microservicio |
|----|---------------|---------------|
| HU-1 | Registrar tecnologías | `api-technology` |
| HU-2 | Registrar capacidades | `api-capability` (consume `api-technology`) |
| HU-3 | Listar capacidades | `api-capability` (consume `api-technology`) |
| HU-4 | Registrar bootcamp | `api-bootcamp` (consume `api-capability`) |
| HU-5 | Listar bootcamps | `api-bootcamp` (consume `api-capability` y `api-technology`) |
| HU-6 | Eliminar bootcamp | `api-bootcamp` (consume `api-capability` y `api-technology`) |
| HU-7 | Inscribirme en bootcamps | `api-persona` (consume `api-bootcamp`) |
| HU-8 | Registrar reporte de bootcamp | `api-reporte` (evento desde `api-bootcamp`) |
| HU-9 | Mostrar bootcamp con mayor personas | `api-reporte` (consume `api-bootcamp` y `api-persona`) |

---

## Guía de Operadores de WebFlux (Project Reactor)

### Conceptos base

- **`Mono<T>`**: Emite 0 o 1 elemento. Equivalente reactivo a un `Optional` o una `Promise`.
- **`Flux<T>`**: Emite 0 a N elementos. Equivalente reactivo a un `List` o `Stream`.
- **Nada se ejecuta hasta que alguien se suscribe**. Los operadores solo definen el pipeline.

---

### 1. Transformación

#### `map` — Transformación síncrona

Transforma el valor emitido sin hacer operaciones asíncronas.

```java
// Extraer un campo
Mono<String> name = technologyMono.map(tech -> tech.getName());

// Convertir DTO a dominio
Mono<Technology> domain = dtoMono.map(dto -> mapper.toDomain(dto));
```

**Usar cuando**: el resultado es inmediato (no involucra DB, HTTP, ni otro Mono/Flux).

---

#### `flatMap` — Transformación asíncrona

Transforma el valor en otro `Mono` o `Flux`. Es el operador más usado.

```java
// Buscar en DB con el valor anterior
Mono<Technology> result = idMono.flatMap(id -> repository.findById(id));

// Encadenar operaciones asíncronas
Mono<Capability> saved = validateCapability(cap)
    .flatMap(valid -> repository.save(valid));
```

**Usar cuando**: necesitas hacer otra operación reactiva con el valor (llamar DB, HTTP, otro servicio).

---

#### `flatMapMany` — De Mono a Flux

Transforma un `Mono` en un `Flux` (de un valor salen muchos).

```java
// De un usuario, obtener sus tecnologías
Mono<User> user = userRepository.findById(id);
Flux<Technology> techs = user.flatMapMany(u -> techRepository.findByUserId(u.getId()));
```

---

### 2. Secuenciamiento

#### `then` — Ejecutar después (descartando el valor)

Espera a que el Mono anterior complete y ejecuta otro. No pasa el valor.

```java
// Guardar y luego limpiar caché (no necesitas el resultado del save)
repository.save(entity)
    .then(cacheService.evict("technologies"));

// Validar existencia y luego guardar (el resultado de validar no importa)
technologyService.validateTechnologiesExist(ids)
    .then(capabilityRepository.save(capability));
```

**Usar cuando**: solo te importa que el paso anterior termine, no su resultado.

---

#### `thenReturn` — Ejecutar después y retornar un valor fijo

Como `then` pero retorna un valor que ya tienes.

```java
// Guardar relación (Mono<Void>) pero retornar la capability
technologyService.saveRelationship(id, techIds)
    .thenReturn(savedCapability);

// Borrar y retornar mensaje
repository.deleteById(id)
    .thenReturn("Eliminado exitosamente");
```

**Usar cuando**: la operación retorna `Void` pero necesitas devolver algo al caller.

---

#### `thenMany` — Ejecutar después y retornar un Flux

```java
// Después de limpiar, retornar todos los elementos
repository.deleteAll()
    .thenMany(repository.findAll());
```

---

### 3. Manejo de vacío / ausencia

#### `switchIfEmpty` — Fallback cuando no hay resultado

Si el Mono no emite nada (está vacío), usa este otro Mono.

```java
// Si no existe, crear uno nuevo
repository.findByName(name)
    .switchIfEmpty(Mono.defer(() -> repository.save(newEntity)));

// Si no existe, lanzar error
repository.findById(id)
    .switchIfEmpty(Mono.error(new NotFoundException("Not found")));
```

**Importante**: Siempre usar `Mono.defer()` dentro de `switchIfEmpty` si el fallback tiene side-effects (como guardar en DB). Sin `defer`, el código se ejecuta eagerly al construir la cadena.

---

#### `defaultIfEmpty` — Valor por defecto simple

```java
// Retornar 0 si no hay conteo
repository.countByStatus("active")
    .defaultIfEmpty(0L);
```

**Usar cuando**: el fallback es un valor fijo, no una operación asíncrona.

---

#### `Mono.defer` — Evaluación lazy

Retrasa la creación del Mono hasta que alguien se suscriba.

```java
// SIN defer: repository.save() se invoca SIEMPRE al construir la cadena
.switchIfEmpty(repository.save(entity))  // ❌ se ejecuta aunque findByName encuentre algo

// CON defer: repository.save() solo se invoca si realmente está vacío
.switchIfEmpty(Mono.defer(() -> repository.save(entity)))  // ✅ lazy
```

---

### 4. Manejo de errores

#### `onErrorResume` — Capturar error y recuperarse

Intercepta el error y retorna un Mono alternativo.

```java
// Rollback: si falla guardar relación, borrar la capability y re-lanzar error
technologyService.saveRelationship(id, techIds)
    .thenReturn(saved)
    .onErrorResume(error -> capabilityRepository.deleteById(saved.getId())
        .then(Mono.error(error))
    );

// Fallback: si el servicio externo falla, usar caché
externalService.getData()
    .onErrorResume(e -> cacheService.getCachedData());

// Filtrar por tipo de error
mono.onErrorResume(TimeoutException.class, e -> Mono.just(defaultValue));
```

---

#### `onErrorMap` — Transformar un error en otro

```java
// Envolver excepción técnica en excepción de negocio
webClient.get().retrieve().bodyToMono(String.class)
    .onErrorMap(WebClientResponseException.class,
        e -> new BusinessException("Technology service unavailable"));
```

**Usar cuando**: quieres cambiar el tipo de excepción (para que las capas superiores no conozcan detalles técnicos).

---

#### `onErrorReturn` — Valor fijo ante error

```java
// Si falla, retornar false
technologyService.existAllByIds(ids)
    .onErrorReturn(false);
```

---

#### `doOnError` — Side-effect al ocurrir error (no cambia el flujo)

```java
// Solo loguear, el error sigue propagándose
repository.save(entity)
    .doOnError(e -> log.error("Error guardando: {}", e.getMessage()));
```

---

### 5. Side-effects (observar sin modificar)

Estos operadores **no cambian** el flujo. Son para logging, métricas, auditoría.

| Operador | Se ejecuta cuando... |
|----------|---------------------|
| `doOnNext` | Se emite un valor |
| `doOnError` | Ocurre un error |
| `doOnSuccess` | Completa (con o sin valor) |
| `doOnSubscribe` | Alguien se suscribe |
| `doFinally` | Siempre al terminar (como try-finally) |

```java
repository.save(entity)
    .doOnNext(saved -> log.info("Guardado con id: {}", saved.getId()))
    .doOnError(e -> log.error("Falló el guardado", e))
    .doFinally(signal -> metrics.recordSave(signal));
```

---

### 6. Filtrado y condiciones

#### `filter` — Convertir en vacío si no cumple condición

```java
// Si la tecnología no está activa, el Mono queda vacío
repository.findById(id)
    .filter(tech -> tech.isActive())
    .switchIfEmpty(Mono.error(new IllegalArgumentException("Technology is inactive")));
```

#### `filterWhen` — Filtro con condición asíncrona

```java
// Filtrar solo si el servicio externo confirma
repository.findById(id)
    .filterWhen(tech -> externalService.isAvailable(tech.getId()));
```

---

### 7. Combinación de múltiples fuentes

#### `Mono.zip` — Ejecutar en paralelo y combinar resultados

```java
// Dos llamadas en paralelo, esperar ambas
Mono.zip(
    technologyService.findById(techId),
    capabilityService.findById(capId)
).map(tuple -> new Response(tuple.getT1(), tuple.getT2()));
```

#### `zipWith` — Combinar con otro Mono

```java
// Enriquecer resultado con datos de otro servicio
repository.findById(id)
    .zipWith(statsService.getStats(id))
    .map(tuple -> new TechWithStats(tuple.getT1(), tuple.getT2()));
```

#### `Mono.when` — Esperar que todos completen (sin valores)

```java
// Ejecutar varias operaciones void en paralelo
Mono.when(
    cacheService.evict("tech"),
    auditService.log("deleted", id),
    notificationService.notify(userId)
);
```

---

### 8. Creación de Mono/Flux

| Método | Qué produce |
|--------|-------------|
| `Mono.just(value)` | Mono que emite ese valor |
| `Mono.empty()` | Mono vacío (sin valor, completa inmediatamente) |
| `Mono.error(exception)` | Mono que emite un error |
| `Mono.defer(() -> ...)` | Mono que se crea lazy al suscribirse |
| `Flux.fromIterable(list)` | Flux que emite cada elemento de la lista |
| `Flux.just(a, b, c)` | Flux que emite esos valores |

---

### Reglas rápidas: ¿Cuál operador usar?

```
¿Necesitas transformar el valor?
├── ¿La transformación es síncrona? → map
└── ¿La transformación es asíncrona (DB, HTTP)? → flatMap

¿No necesitas el valor anterior?
├── ¿Solo quieres que termine? → then
└── ¿Quieres retornar algo fijo? → thenReturn

¿El Mono puede estar vacío?
├── ¿El fallback es un valor simple? → defaultIfEmpty
└── ¿El fallback es otra operación? → switchIfEmpty + Mono.defer

¿Hay un error?
├── ¿Quieres recuperarte? → onErrorResume
├── ¿Quieres transformar el error? → onErrorMap
├── ¿Quieres un valor default? → onErrorReturn
└── ¿Solo quieres loguear? → doOnError

¿Quieres observar sin modificar?
└── doOnNext / doOnError / doFinally

¿Necesitas combinar múltiples Monos?
├── ¿En paralelo con resultados? → Mono.zip
└── ¿En paralelo sin resultados? → Mono.when
```

---

### Ejemplo completo: Flujo de creación de Capability

```java
@Override
public Mono<Capability> save(Capability capability) {
    return validateCapability(capability)                          // 1. Validar campos
            .flatMap(cap -> technologyServicePort                  // 2. Verificar tecnologías existen
                    .validateTechnologiesExist(cap.getTechnologyIds())
                    .then(capabilityPersistencePort.findByName(cap.getName())  // 3. Verificar nombre único
                            .flatMap(exist -> Mono.<Capability>error(
                                new IllegalArgumentException("Cap name already exist")))
                            .switchIfEmpty(Mono.defer(() ->
                                capabilityPersistencePort.save(cap)))          // 4. Guardar capability
                    )
            )
            .flatMap(saved -> technologyServicePort                // 5. Guardar relación
                    .saveCapabilityTechnologies(saved.getId(), saved.getTechnologyIds())
                    .thenReturn(saved)                             // 6. Retornar capability guardada
                    .onErrorResume(error ->                        // 7. Rollback si falla
                        capabilityPersistencePort.deleteById(saved.getId())
                            .then(Mono.error(error))
                    )
            );
}
```

**Lectura en español**:
1. Valida los campos (nombre, descripción, cantidad de tecnologías)
2. Verifica que todas las tecnologías existan en api-technology (1 sola petición HTTP)
3. Verifica que no exista otra capability con el mismo nombre
4. Si no existe → guarda la capability en la DB local
5. Guarda la relación capability-tecnología en api-technology
6. Si todo sale bien → retorna la capability guardada
7. Si el paso 5 falla → borra la capability (rollback manual) y propaga el error
