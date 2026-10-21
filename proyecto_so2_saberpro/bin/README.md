# Banco de Preguntas Saber Pro

![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)
![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A22.svg)
![SQLite](https://img.shields.io/badge/SQLite-Database-003B57.svg)

Sistema de gestión y revisión de preguntas para simulacros tipo Saber Pro. Esta aplicación de escritorio desarrollada en Java (Swing) permite administrar usuarios y centralizar el proceso de creación, validación y almacenamiento de preguntas para pruebas estandarizadas.

## Características Principales

- **Arquitectura en Capas:** Diseño estructurado en 3 capas (Presentación, Dominio, Acceso a Datos) para garantizar la escalabilidad y mantenibilidad.
- **Gestión de Usuarios con Roles:** Sistema robusto de inicio de sesión y registro (contraseñas hasheadas con Argon2). Soporta múltiples roles:
  - *Administrador*: Gestiona usuarios y asigna revisores a preguntas.
  - *Autor de Preguntas*: Crea y propone nuevas preguntas.
  - *Revisor*: Valida técnica y gramaticalmente las preguntas.
  - *Docente*: (Próximamente) Crea simulacros y evalúa estudiantes.
  - *Estudiante*: (Próximamente) Realiza simulacros.
- **Validaciones Estructurales (HU-03):** Reglas de negocio estrictas al crear preguntas para garantizar la calidad:
  - Bloqueo de expresiones redundantes ("Todas las anteriores", "Ninguna de las anteriores").
  - Validación de longitud y estructura gramatical de los distractores.
  - Obligatoriedad de justificación y contexto.
- **Interfaz Moderna:** Diseño renovado de Swing que implementa utilidades visuales (`UIUtils`) para botones con efectos *hover*, tablas estilizadas, colores consistentes y una tipografía moderna sin dependencias externas.
- **Persistencia Local:** Uso de SQLite para el almacenamiento de datos, facilitando un entorno ligero sin necesidad de instalar motores de base de datos pesados.

## Estructura del Proyecto

El proyecto sigue una estructura de módulos por paquetes, dividiendo el sistema de **Usuarios** del sistema del **Banco de Preguntas**.

```text
src/main/java/
├── co/edu/unicauca/bancopreguntas/
│   ├── dataaccess/       # Repositorios y conexión SQLite (Preguntas)
│   ├── domain/           # Entidades y lógica de negocio (Validaciones)
│   ├── presentation/     # Vistas (Crear, Listar, Detalle) y Controladores
│   └── Main.java         # Punto de entrada de la aplicación
└── com/unicauca/taller2/usuarios/
    ├── access/           # Repositorios y políticas de contraseñas
    ├── model/            # Entidades de Usuario y Rol
    ├── presentation/     # Vistas (Login, Registro, Dashboard, Gestión)
    └── services/         # Servicios de Autenticación y Usuarios
```

## Instalación y Ejecución

### Prerrequisitos

- **Java Development Kit (JDK) 17** o superior.
- **Maven** (el proyecto incluye el wrapper `mvnw` por lo que no es estrictamente necesario tenerlo instalado globalmente).

### Pasos para ejecutar

1. Clona el repositorio:
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd proyecto_so2_saberpro
   ```

2. Compila el proyecto (descargará las dependencias necesarias como SQLite JDBC, Argon2 y JUnit):
   ```bash
   ./mvnw clean compile
   ```

3. Ejecuta la aplicación:
   ```bash
   ./mvnw exec:java
   ```
   *(Nota: En Windows, utiliza `.\mvnw.cmd` en lugar de `./mvnw`)*

Al ejecutar el proyecto por primera vez, el sistema **creará automáticamente la base de datos `saberpro.db`** y las tablas necesarias en la raíz del proyecto. El usuario administrador por defecto es **`admin`** con contraseña **`Admin123*`** (o según las instrucciones de tu práctica).

## Pruebas Unitarias

El proyecto cuenta con pruebas unitarias implementadas con **JUnit 5** y **Mockito** para garantizar el correcto funcionamiento de los servicios del dominio (como las validaciones de preguntas).

Para ejecutar las pruebas:
```bash
./mvnw test
```

## Tecnologías Utilizadas

- **Lenguaje:** Java 17
- **Interfaz Gráfica:** Java Swing
- **Base de Datos:** SQLite (v3.47.1)
- **Gestión de Dependencias:** Maven
- **Seguridad:** Argon2 (Hashing de contraseñas)
- **Testing:** JUnit 5, Mockito

---
*Proyecto desarrollado para la asignatura de Ingeniería de Software II (Corte 1).*
