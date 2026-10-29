# Banco de Preguntas Saber Pro
## Taller 10: Persistencia Limpia con JPA y Spring Boot

![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data-JPA-green.svg)
![H2 Database](https://img.shields.io/badge/H2-In--Memory-yellow.svg)
![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A22.svg)

Sistema para la gestión, revisión, publicación y evaluación de simulacros para pruebas tipo **Saber Pro** (Universidad del Cauca). Este laboratorio implementa una **arquitectura por capas con persistencia limpia (Clean Persistence)**, garantizando que el modelo de dominio permanezca 100% puro y completamente desacoplado de las anotaciones tecnológicas de Jakarta Persistence (`jakarta.persistence.*`).

---

## Documentos de Entrega del Taller 10

* **Parte I - Análisis del Demo:** [PARTE_I_ANALISIS_DEMO.md](PARTE_I_ANALISIS_DEMO.md)
  * Tabla 1 completada con componentes y responsabilidades.
  * Respuestas justificadas a las actividades 1.1, 1.2, 1.3 y 1.4.
  * Respuestas a las 6 preguntas de reflexión para la sustentación.
* **Parte II y III - Diseño y Arquitectura:** [DISEÑO_ARQUITECTURA_UML.md](DISEÑO_ARQUITECTURA_UML.md)
  * Diagrama de clases UML del dominio.
  * Diagrama entidad-relación (ERD) del modelo de base de datos relacional.
  * Mapeo de los 4 tipos de relaciones y herencia JOINED.

---

## Principios y Reglas de la Arquitectura

1. **Dominio Puro (Zero JPA):** Ninguna clase en el paquete `com.example.questionbank.domain` importa `jakarta.persistence.*` ni posee anotaciones como `@Entity`, `@Table`, `@Id`, `@OneToMany` o `@ManyToMany`.
2. **Inversión de Dependencias (DIP):** Los servicios de aplicación (`QuestionService`, `ReviewService`, `QuizService`) dependen exclusivamente de las **interfaces de repositorio de dominio** (`QuestionRepository`, `UserRepository`, `QuizRepository`), nunca de `JpaRepository` ni de clases concretas de infraestructura.
3. **Persistencia Tecnológica (`infrastructure.jpa`):** La infraestructura contiene las entidades JPA (`*Record`), las interfaces de Spring Data JPA (`Spring*DataRepository`) y las implementaciones concretas de los repositorios (`*RepositoryJpa`), traduciendo los datos mediante el `DomainMapper`.
4. **Herencia con Estrategia `JOINED`:** La jerarquía de preguntas (`QuestionRecord`, `MultipleChoiceQuestionRecord`, `TrueFalseQuestionRecord`) se mapea en tablas relacionales especializadas (`questions`, `mc_questions`, `tf_questions`) preservando la normalización y la integridad referencial.
5. **Reglas de Negocio en Simulacros:** **Solo preguntas aprobadas y publicadas (`PUBLISHED`) pueden agregarse a un cuestionario.** Agregar una pregunta en borrador o pendiente de revisión lanza una excepción de dominio (`IllegalStateException`).

---

## Estructura de Paquetes

```text
src/main/java/com/example/questionbank/
├── QuestionBankApplication.java       # Punto de entrada Spring Boot
├── api/                               # Capa de Presentación (REST)
│   ├── QuestionController.java        # Endpoints para creación, consulta y revisión
│   ├── QuizController.java            # Endpoints para simulacros y cuestionarios
│   └── dto/                           # Data Transfer Objects (DTOs)
├── application/                       # Capa de Aplicación (Casos de Uso)
│   ├── QuestionService.java           # Orquestación de preguntas (UC 1, 2, 3, 4)
│   ├── ReviewService.java             # Flujo de revisión y evaluación
│   └── QuizService.java               # Creación de simulacros con validación (UC 5)
├── domain/                            # Capa de Dominio (Sin dependencias externas)
│   ├── model/
│   │   ├── Question.java              # Clase base abstracta con ciclo de vida
│   │   ├── MultipleChoiceQuestion.java# Preguntas de selección múltiple (opciones)
│   │   ├── TrueFalseQuestion.java     # Preguntas de Falso/Verdadero
│   │   ├── AnswerOption.java          # Opciones de respuesta y distractores
│   │   ├── QuestionMetadata.java      # Metadatos complementarios (1 a 1)
│   │   ├── User.java                  # Usuarios del sistema (Autor, Revisor, etc.)
│   │   ├── Competency.java            # Competencias académicas evaluadas
│   │   ├── Topic.java                 # Temas temáticos
│   │   ├── Quiz.java                  # Cuestionarios / Simulacros
│   │   ├── Review.java                # Revisiones técnicas y pedagógicas
│   │   └── enums...                   # Role, Difficulty, QuestionStatus, CognitiveLevel
│   └── repository/                    # Contratos independientes de tecnología
│       ├── QuestionRepository.java
│       ├── UserRepository.java
│       ├── CompetencyRepository.java
│       ├── TopicRepository.java
│       ├── QuizRepository.java
│       └── ReviewRepository.java
└── infrastructure/                    # Capa de Infraestructura (JPA / H2)
    └── jpa/
        ├── record/                    # Entidades tecnológicas JPA (@Entity)
        │   ├── QuestionRecord.java    # @Inheritance(strategy = InheritanceType.JOINED)
        │   ├── MultipleChoiceQuestionRecord.java
        │   ├── TrueFalseQuestionRecord.java
        │   ├── AnswerOptionRecord.java
        │   ├── QuestionMetadataRecord.java
        │   ├── UserRecord.java
        │   ├── CompetencyRecord.java
        │   ├── TopicRecord.java
        │   ├── QuizRecord.java
        │   └── ReviewRecord.java
        ├── repository/                # Interfaces Spring Data JPA
        │   ├── SpringQuestionDataRepository.java
        │   ├── SpringMultipleChoiceQuestionDataRepository.java
        │   ├── SpringTrueFalseQuestionDataRepository.java
        │   ├── SpringUserDataRepository.java
        │   ├── SpringCompetencyDataRepository.java
        │   ├── SpringTopicDataRepository.java
        │   ├── SpringQuizDataRepository.java
        │   └── SpringReviewDataRepository.java
        ├── mapper/
        │   └── DomainMapper.java      # Conversión bidireccional Dominio <-> JPA
        ├── QuestionRepositoryJpa.java # Implementación concreta de QuestionRepository
        ├── UserRepositoryJpa.java
        ├── CompetencyRepositoryJpa.java
        ├── TopicRepositoryJpa.java
        ├── QuizRepositoryJpa.java
        └── ReviewRepositoryJpa.java
```

---

## Mapeo de Relaciones JPA (Punto 4 del Taller)

| Tipo de Relación | Clases Mapeadas | Lado Propietario | Clave Foránea / Join Table |
| :--- | :--- | :--- | :--- |
| **`@OneToOne`** | `QuestionRecord` $\leftrightarrow$ `QuestionMetadataRecord` | `QuestionRecord` | `metadata_id` con restricción `UNIQUE` en tabla `questions` |
| **`@OneToMany`** | `MultipleChoiceQuestionRecord` $\leftrightarrow$ `AnswerOptionRecord` | `AnswerOptionRecord` | `question_id` en tabla `answer_options` (`nullable = false`) |
| **`@ManyToOne`** | `QuestionRecord` $\rightarrow$ `UserRecord` (autor) y `TopicRecord` | `QuestionRecord` | `author_id` y `topic_id` en tabla `questions` |
| **`@ManyToMany`** | `QuestionRecord` $\leftrightarrow$ `CompetencyRecord` | `QuestionRecord` | Tabla de unión `question_competencies` |
| **`@ManyToMany`** | `QuizRecord` $\leftrightarrow$ `QuestionRecord` | `QuizRecord` | Tabla de unión `quiz_questions` |

---

## Ejecución y Pruebas

### Prerrequisitos
* **Java Development Kit (JDK) 17** o superior.
* Maven (o usar el wrapper incluido `./mvnw.cmd` / `./mvnw`).

### 1. Ejecución de Pruebas Automatizadas
Para ejecutar las pruebas unitarias y de integración:

```bash
# En Windows:
.\mvnw.cmd test

# En Linux / macOS:
./mvnw test
```

El conjunto de pruebas ejecuta **150 pruebas unitarias y de integración**, verificando:
* Pruebas de reglas de negocio en el dominio puro (`QuestionDomainTest`, `QuizDomainTest`).
* Pruebas de persistencia JPA con H2 en memoria (`CleanJpaPersistenceIntegrationTest`).
* Pruebas de los 5 casos de uso de extremo a extremo (`QuestionBankUseCasesIntegrationTest`).
* Pruebas de endpoints REST con MockMvc (`QuestionControllerIntegrationTest`).
* Compatibilidad con todas las pruebas existentes de talleres previos.

### 2. Iniciar la Aplicación Spring Boot
Para levantar la aplicación web:

```bash
# En Windows:
.\mvnw.cmd spring-boot:run

# En Linux / macOS:
./mvnw spring-boot:run
```

La aplicación iniciará en el puerto **8080**.

### 3. Consola Web H2
Una vez levantada la aplicación, se puede inspeccionar la base de datos relacional y las tablas creadas en el navegador:
* **URL:** `http://localhost:8080/h2-console`
* **JDBC URL:** `jdbc:h2:mem:questionbank`
* **Usuario:** `sa`
* **Contraseña:** *(dejar en blanco)*

---

## Ejemplos de Uso de la API REST

### 1. Crear Pregunta de Selección Múltiple (Caso de Uso 1)
`POST /api/questions/multiple-choice`
```json
{
  "statement": "¿Cuál de los siguientes patrones desacopla una abstracción de su implementación?",
  "difficulty": "MEDIUM",
  "authorId": 1,
  "options": [
    { "text": "Bridge", "correct": true, "explanation": "Separa la abstracción de su implementación" },
    { "text": "Singleton", "correct": false, "explanation": "Garantiza una única instancia" },
    { "text": "Observer", "correct": false, "explanation": "Define dependencia uno a muchos" },
    { "text": "Decorator", "correct": false, "explanation": "Añade responsabilidades dinámicamente" }
  ],
  "metadata": {
    "cognitiveLevel": "ANALYZE",
    "estimatedTimeMinutes": 10,
    "editorialNotes": "Pregunta de diseño de software Saber Pro."
  }
}
```

### 2. Clasificar Pregunta (Caso de Uso 3)
`POST /api/questions/1/classify`
```json
{
  "topicId": 1,
  "competencyIds": [1, 2]
}
```

### 3. Enviar a Revisión (Caso de Uso 4)
`POST /api/questions/1/send-to-review`

### 4. Crear Cuestionario / Simulacro (Caso de Uso 5)
`POST /api/quizzes`
```json
{
  "title": "Simulacro Saber Pro 2026 - Ingeniería de Software",
  "description": "Simulacro oficial de preparación",
  "createdById": 1,
  "questionIds": [1, 2],
  "totalScore": 100
}
```
*(Nota: Si alguna pregunta en `questionIds` no está en estado `PUBLISHED`, el sistema responderá `400 Bad Request` indicando la violación de la regla de negocio).*
