package co.edu.unicauca.bancopreguntas.users;

import co.edu.unicauca.bancopreguntas.contracts.dto.TokenResponseDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.users.domain.EstadoUsuario;
import co.edu.unicauca.bancopreguntas.users.domain.Rol;
import co.edu.unicauca.bancopreguntas.users.domain.UsuarioEntity;
import co.edu.unicauca.bancopreguntas.users.repository.UsuarioRepository;
import co.edu.unicauca.bancopreguntas.users.security.JwtUtil;
import co.edu.unicauca.bancopreguntas.users.security.PasswordHasher;
import co.edu.unicauca.bancopreguntas.users.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioServiceTest {

    private StubUsuarioRepository repository;
    private StubPasswordHasher hasher;
    private JwtUtil jwtUtil;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        repository = new StubUsuarioRepository();
        hasher = new StubPasswordHasher();
        jwtUtil = new JwtUtil("SaberProSecretKey2026SuperSecureMustBeAtLeast256BitsLongForHMACSHA256", 3600000);
        service = new UsuarioService(repository, hasher, jwtUtil);
    }

    @Test
    @DisplayName("Autenticación exitosa con credenciales válidas")
    void autenticar_exitoso() {
        UsuarioEntity user = new UsuarioEntity(1L, "juan12", "Juan Pérez", "juan@unicauca.edu.co",
                Rol.AUTOR, EstadoUsuario.ACTIVO, "hash123", LocalDateTime.now());
        repository.save(user);
        hasher.setResult(true);

        TokenResponseDTO response = service.autenticar("juan12", "Password123!@");

        assertNotNull(response);
        assertEquals("juan12", response.getNombreUsuario());
        assertEquals("AUTOR", response.getRol());
        assertNotNull(response.getToken());
    }

    @Test
    @DisplayName("Autenticación fallida por contraseña incorrecta")
    void autenticar_passwordInvalido() {
        UsuarioEntity user = new UsuarioEntity(1L, "juan12", "Juan Pérez", "juan@unicauca.edu.co",
                Rol.AUTOR, EstadoUsuario.ACTIVO, "hash123", LocalDateTime.now());
        repository.save(user);
        hasher.setResult(false);

        assertThrows(IllegalArgumentException.class, () -> service.autenticar("juan12", "Incorrecta"));
    }

    @Test
    @DisplayName("Autenticación rechazada para usuario inactivo")
    void autenticar_usuarioInactivo() {
        UsuarioEntity user = new UsuarioEntity(1L, "pepe12", "Pepito", "pepe@unicauca.edu.co",
                Rol.ADMINISTRADOR, EstadoUsuario.INACTIVO, "hash123", LocalDateTime.now());
        repository.save(user);

        assertThrows(IllegalStateException.class, () -> service.autenticar("pepe12", "Password123!@"));
    }

    @Test
    @DisplayName("Listar revisores activos")
    void listarRevisores_retornaSoloRevisoresActivos() {
        UsuarioEntity revisor1 = new UsuarioEntity(6L, "Juanita", "Juana Pérez", "juanita@unicauca.edu.co",
                Rol.REVISOR, EstadoUsuario.ACTIVO, "hash", LocalDateTime.now());
        UsuarioEntity revisor2 = new UsuarioEntity(10L, "Juan6", "Juan García", "juan6@unicauca.edu.co",
                Rol.REVISOR, EstadoUsuario.ACTIVO, "hash", LocalDateTime.now());
        UsuarioEntity autor = new UsuarioEntity(1L, "juan12", "Juan Autor", "juan12@unicauca.edu.co",
                Rol.AUTOR, EstadoUsuario.ACTIVO, "hash", LocalDateTime.now());

        repository.save(revisor1);
        repository.save(revisor2);
        repository.save(autor);

        List<UserSummaryDTO> revisores = service.listarRevisores();

        assertEquals(2, revisores.size());
        assertEquals("Juanita", revisores.get(0).getNombreUsuario());
        assertEquals("Juan6", revisores.get(1).getNombreUsuario());
    }

    private static class StubPasswordHasher implements PasswordHasher {
        private boolean result = true;
        public void setResult(boolean result) { this.result = result; }

        @Override
        public String hash(String rawPassword) {
            return "hashed_" + rawPassword;
        }

        @Override
        public boolean verify(String rawPassword, String hashedPassword) {
            return result;
        }
    }

    private static class StubUsuarioRepository implements UsuarioRepository {
        private final Map<Long, UsuarioEntity> store = new ConcurrentHashMap<>();

        @Override
        public Optional<UsuarioEntity> findByNombreUsuarioIgnoreCase(String nombreUsuario) {
            return store.values().stream()
                    .filter(u -> u.getNombreUsuario().equalsIgnoreCase(nombreUsuario))
                    .findFirst();
        }

        @Override
        public List<UsuarioEntity> findByEstado(EstadoUsuario estado) {
            return store.values().stream()
                    .filter(u -> u.getEstado() == estado)
                    .toList();
        }

        @Override
        public List<UsuarioEntity> findByRolAndEstado(Rol rol, EstadoUsuario estado) {
            return store.values().stream()
                    .filter(u -> u.getRol() == rol && u.getEstado() == estado)
                    .toList();
        }

        @Override
        public <S extends UsuarioEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId((long) (store.size() + 1));
            }
            store.put(entity.getId(), entity);
            return entity;
        }

        @Override public Optional<UsuarioEntity> findById(Long aLong) { return Optional.ofNullable(store.get(aLong)); }
        @Override public boolean existsById(Long aLong) { return store.containsKey(aLong); }
        @Override public List<UsuarioEntity> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<UsuarioEntity> findAllById(Iterable<Long> longs) { return Collections.emptyList(); }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) { store.remove(aLong); }
        @Override public void delete(UsuarioEntity entity) { store.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends UsuarioEntity> entities) {}
        @Override public void deleteAll() { store.clear(); }
        @Override public <S extends UsuarioEntity> List<S> saveAll(Iterable<S> entities) { return Collections.emptyList(); }
        @Override public void flush() {}
        @Override public <S extends UsuarioEntity> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends UsuarioEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return Collections.emptyList(); }
        @Override public void deleteAllInBatch(Iterable<UsuarioEntity> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public UsuarioEntity getOne(Long aLong) { return store.get(aLong); }
        @Override public UsuarioEntity getById(Long aLong) { return store.get(aLong); }
        @Override public UsuarioEntity getReferenceById(Long aLong) { return store.get(aLong); }
        @Override public <S extends UsuarioEntity> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends UsuarioEntity> List<S> findAll(Example<S> example) { return Collections.emptyList(); }
        @Override public <S extends UsuarioEntity> List<S> findAll(Example<S> example, Sort sort) { return Collections.emptyList(); }
        @Override public <S extends UsuarioEntity> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends UsuarioEntity> long count(Example<S> example) { return 0; }
        @Override public <S extends UsuarioEntity> boolean exists(Example<S> example) { return false; }
        @Override public <S extends UsuarioEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<UsuarioEntity> findAll(Sort sort) { return new ArrayList<>(store.values()); }
        @Override public Page<UsuarioEntity> findAll(Pageable pageable) { return Page.empty(); }
    }
}
