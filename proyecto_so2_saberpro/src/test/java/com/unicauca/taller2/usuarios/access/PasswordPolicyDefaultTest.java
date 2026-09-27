package com.unicauca.taller2.usuarios.access;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para PasswordPolicyDefault.
 */
class PasswordPolicyDefaultTest {

    private PasswordPolicyDefault policy;

    @BeforeEach
    void setUp() {
        policy = new PasswordPolicyDefault();
    }

    @ParameterizedTest
    @DisplayName("Contraseñas válidas: cumplen todas las reglas")
    @ValueSource(strings = {
            "Abc12!",
            "Password1!",
            "A1b2c3!@#",
            "ABCDEF1!",
            "Hola Mundo1!",
            "Z9!zzz",
            "abcdeF1@"
    })
    void validar_contraseñaValida_noLanzaExcepcion(String password) {
        assertDoesNotThrow(() -> policy.validar(password));
    }

    @Test
    @DisplayName("Rechazar: contraseña null")
    void validar_null_lanzaExcepcion() {
        assertThrows(PasswordPolicyException.class, () -> policy.validar(null));
    }

    @Test
    @DisplayName("Rechazar: contraseña vacía")
    void validar_vacia_lanzaExcepcion() {
        PasswordPolicyException ex = assertThrows(PasswordPolicyException.class,
                () -> policy.validar(""));
        assertTrue(ex.getMessage().contains("6 caracteres"));
    }

    @Test
    @DisplayName("Rechazar: contraseña con 5 caracteres (justo por debajo del mínimo)")
    void validar_5caracteres_lanzaExcepcion() {
        PasswordPolicyException ex = assertThrows(PasswordPolicyException.class,
                () -> policy.validar("Ab1!x"));
        assertTrue(ex.getMessage().contains("6 caracteres"));
    }

    @Test
    @DisplayName("Rechazar: sin dígito")
    void validar_sinDigito_lanzaExcepcion() {
        PasswordPolicyException ex = assertThrows(PasswordPolicyException.class,
                () -> policy.validar("Abcdef!@"));
        assertTrue(ex.getMessage().contains("dígito"));
    }

    @Test
    @DisplayName("Rechazar: sin mayúscula")
    void validar_sinMayuscula_lanzaExcepcion() {
        PasswordPolicyException ex = assertThrows(PasswordPolicyException.class,
                () -> policy.validar("abc123!@"));
        assertTrue(ex.getMessage().contains("mayúscula"));
    }

    @Test
    @DisplayName("Rechazar: sin carácter especial")
    void validar_sinCaracterEspecial_lanzaExcepcion() {
        PasswordPolicyException ex = assertThrows(PasswordPolicyException.class,
                () -> policy.validar("Abc12345"));
        assertTrue(ex.getMessage().contains("especial"));
    }

    @Test
    @DisplayName("Rechazar: múltiples reglas incumplidas (solo minúsculas)")
    void validar_multiplesFallos_reportaTodos() {
        PasswordPolicyException ex = assertThrows(PasswordPolicyException.class,
                () -> policy.validar("abcdef"));
        String mensaje = ex.getMessage();
        assertTrue(mensaje.contains("dígito"));
        assertTrue(mensaje.contains("mayúscula"));
        assertTrue(mensaje.contains("especial"));
    }

    @Test
    @DisplayName("Rechazar: contraseña con solo dígitos")
    void validar_soloDigitos_reportaFaltas() {
        PasswordPolicyException ex = assertThrows(PasswordPolicyException.class,
                () -> policy.validar("123456"));
        String mensaje = ex.getMessage();
        assertTrue(mensaje.contains("mayúscula"));
        assertTrue(mensaje.contains("especial"));
    }
}
