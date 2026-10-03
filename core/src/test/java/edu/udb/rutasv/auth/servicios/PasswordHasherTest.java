package edu.udb.rutasv.auth.servicios;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class PasswordHasherTest {
    private final PasswordHasher hasher = new PasswordHasher(1000);

    @Test
    void verificaContrasenaCorrecta() {
        String sal = hasher.nuevaSalBase64();
        String hash = hasher.hashear("clave-segura-1", sal);
        assertTrue(hasher.verificar("clave-segura-1", sal, hash));
    }

    @Test
    void rechazaContrasenaIncorrecta() {
        String sal = hasher.nuevaSalBase64();
        String hash = hasher.hashear("clave-segura-1", sal);
        assertFalse(hasher.verificar("clave-segura-2", sal, hash));
    }

    @Test
    void salesDistintasDanHashesDistintos() {
        String s1 = hasher.nuevaSalBase64();
        String s2 = hasher.nuevaSalBase64();
        assertNotEquals(s1, s2);
        assertNotEquals(hasher.hashear("abcdefgh", s1), hasher.hashear("abcdefgh", s2));
    }

    @Test
    void salDe16BytesYHashDe32() {
        String sal = hasher.nuevaSalBase64();
        assertEquals(16, Base64.getDecoder().decode(sal).length);
        assertEquals(32, Base64.getDecoder().decode(hasher.hashear("abcdefgh", sal)).length);
    }

    @Test
    void iteracionesInvalidasFallan() {
        assertThrows(IllegalArgumentException.class, () -> new PasswordHasher(0));
    }

    @Test
    void toStringNoFiltraNada() {
        assertFalse(hasher.toString().contains("abcdefgh"));
    }
}
