package co.edu.unicauca.bancopreguntas.users;

import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.users.domain.EstadoUsuario;
import co.edu.unicauca.bancopreguntas.users.domain.Rol;
import co.edu.unicauca.bancopreguntas.users.domain.UsuarioEntity;
import co.edu.unicauca.bancopreguntas.users.repository.UsuarioRepository;
import co.edu.unicauca.bancopreguntas.users.security.PasswordHasher;
import co.edu.unicauca.bancopreguntas.users.security.ResourceServerSecurityConfig;
import co.edu.unicauca.bancopreguntas.users.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de UsuarioService y mapeo de roles Keycloak (HU10.S3.1 / HU7.S2.3).
 */
class UsuarioServiceTest {

    private StubUsuarioRepository repository;
    private StubPasswordHasher hasher;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        repository = new StubUsuarioRepository();
        hasher = new StubPasswordHasher();
        service = new UsuarioService(repository, hasher);
    }

    @Test
    @DisplayName("Listar revisores activos retorna solo usuarios con rol REVISOR y estado ACTIVO")
    void listarRevisores_retornaSoloRevisoresActivos() {
        UsuarioEntity revisor1 = new UsuarioEntity(6L, "Juanita", "Juana Perez", "juanita@unicauca.edu.co",
                Rol.REVISOR, EstadoUsuario.ACTIVO, "hash", LocalDateTime.now());
        UsuarioEntity revisor2 = new UsuarioEntity(10L, "Juan6", "Juan Garcia", "juan6@unicauca.edu.co",
                Rol.REVISOR, EstadoUsuario.ACTIVO, "hash", LocalDateTime.now());
        UsuarioEntity revisorInactivo = new UsuarioEntity(11L, "Inactivo", "Rev Inactivo", "inact@unicauca.edu.co",
                Rol.REVISOR, EstadoUsuario.INACTIVO, "hash", LocalDateTime.now());
        UsuarioEntity autor = new UsuarioEntity(1L, "juan12", "Juan Autor", "juan12@unicauca.edu.co",
                Rol.AUTOR, EstadoUsuario.ACTIVO, "hash", LocalDateTime.now());

        repository.save(revisor1);
        repository.save(revisor2);
        repository.save(revisorInactivo);
        repository.save(autor);

        List<UserSummaryDTO> revisores = service.listarRevisores();

        assertEquals(2, revisores.size());
        assertTrue(revisores.stream().allMatch(r -> "REVISOR".equals(r.getRol()) && "ACTIVO".equals(r.getEstado())));
    }

    @Test
    @DisplayName("HU10.S3.1 - Registro de usuario nuevo con rol AUTOR")
    void registrar_usuarioNuevo_guardaCorrectamente() {
        UserSummaryDTO dto = service.registrar(
                "nuevoautor", "Nuevo Autor", "nuevo@unicauca.edu.co", "Password1!", Rol.AUTOR
        );
        assertNotNull(dto);
        assertEquals("nuevoautor", dto.getNombreUsuario());
        assertEquals("AUTOR", dto.getRol());
        assertEquals("ACTIVO", dto.getEstado());
        assertEquals(1, repository.count());
    }

    @Test
    @DisplayName("HU10.S3.1 - Registro duplicado lanza IllegalArgumentException")
    void registrar_usuarioDuplicado_lanzaExcepcion() {
        UsuarioEntity existente = new UsuarioEntity(1L, "autor1", "Autor Uno", "autor1@unicauca.edu.co",
                Rol.AUTOR, EstadoUsuario.ACTIVO, "hash123", LocalDateTime.now());
        repository.save(existente);

        assertThrows(IllegalArgumentException.class, () ->
                service.registrar("autor1", "Otro Nombre", "otro@unicauca.edu.co", "Password2!", Rol.AUTOR));
    }

    @Test
    @DisplayName("HU10.S3.1 - Registro con username vacío o rol nulo lanza IllegalArgumentException")
    void registrar_camposInvalidos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                service.registrar("   ", "Nombre", "mail@u.co", "Pass1!", Rol.AUTOR));
        assertThrows(IllegalArgumentException.class, () ->
                service.registrar("userValid", "Nombre", "mail@u.co", "Pass1!", null));
    }

    @Test
    @DisplayName("HU10.S3.1 - Búsqueda por ID retorna DTO con email (usado para notificaciones HU12)")
    void buscarPorId_existente_retornaDTOConEmail() {
        UsuarioEntity u = new UsuarioEntity(5L, "revisor1", "Revisor Uno", "revisor1@unicauca.edu.co",
                Rol.REVISOR, EstadoUsuario.ACTIVO, "h", LocalDateTime.now());
        repository.save(u);

        Optional<UserSummaryDTO> found = service.buscarPorId(5L);

        assertTrue(found.isPresent());
        assertEquals("revisor1@unicauca.edu.co", found.get().getEmail());
        assertEquals("REVISOR", found.get().getRol());
    }

    @Test
    @DisplayName("HU10.S3.1 - Búsqueda por ID inexistente retorna Optional vacío")
    void buscarPorId_inexistente_retornaEmpty() {
        assertTrue(service.buscarPorId(999L).isEmpty());
    }

    @Test
    @DisplayName("HU10.S3.1 - Búsqueda por preferredUsername de Keycloak es case-insensitive")
    void buscarPorUsername_caseInsensitive() {
        UsuarioEntity u = new UsuarioEntity(2L, "Admin1", "Admin Uno", "admin@unicauca.edu.co",
                Rol.ADMINISTRADOR, EstadoUsuario.ACTIVO, "h", LocalDateTime.now());
        repository.save(u);

        Optional<UserSummaryDTO> found = service.buscarPorUsername("admin1");
        assertTrue(found.isPresent());
        assertEquals("ADMINISTRADOR", found.get().getRol());
    }

    @Test
    @DisplayName("HU10.S3.1 - Listar todos retorna usuarios de todos los roles")
    void listarTodos_retornaTodosLosUsuarios() {
        repository.save(new UsuarioEntity(1L, "a1", "Autor Uno", "a1@u.co", Rol.AUTOR, EstadoUsuario.ACTIVO, "h", LocalDateTime.now()));
        repository.save(new UsuarioEntity(2L, "r1", "Revisor Uno", "r1@u.co", Rol.REVISOR, EstadoUsuario.ACTIVO, "h", LocalDateTime.now()));
        repository.save(new UsuarioEntity(3L, "adm", "Admin Uno", "adm@u.co", Rol.ADMINISTRADOR, EstadoUsuario.ACTIVO, "h", LocalDateTime.now()));

        List<UserSummaryDTO> todos = service.listarTodos();
        assertEquals(3, todos.size());
    }

    @Test
    @DisplayName("HU10.S3.1 / HU7 - Roles de dominio y extracción de authorities desde JWT Keycloak (realm_access)")
    void rolesKeycloak_mapeoCorrectoAAuthorities() {
        assertNotNull(Rol.valueOf("AUTOR"));
        assertNotNull(Rol.valueOf("REVISOR"));
        assertNotNull(Rol.valueOf("ADMINISTRADOR"));

        Jwt jwt = new Jwt(
                "token-val", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("alg", "RS256"),
                Map.of("preferred_username", "admin", "realm_access", Map.of("roles", List.of("ADMINISTRADOR", "default-roles-saberpro")))
        );
        Collection<GrantedAuthority> authorities = ResourceServerSecurityConfig.extractAuthorities(jwt);
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMINISTRADOR", authorities.iterator().next().getAuthority());
    }

    // ─── Stubs ───────────────────────────────────────────────────────────────

    private static class StubPasswordHasher implements PasswordHasher {
        @Override
        public String hash(String rawPassword) {
            return "hashed_" + rawPassword;
        }

        @Override
        public boolean verify(String rawPassword, String hashedPassword) {
            return ("hashed_" + rawPassword).equals(hashedPassword);
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
            return store.values().stream().filter(u -> u.getEstado() == estado).toList();
        }

        @Override
        public List<UsuarioEntity> findByRolAndEstado(Rol rol, EstadoUsuario estado) {
            return store.values().stream().filter(u -> u.getRol() == rol && u.getEstado() == estado).toList();
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
