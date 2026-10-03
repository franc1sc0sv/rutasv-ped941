package edu.udb.rutasv.auth.servicios;

import edu.udb.rutasv.auth.modelo.Rol;
import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.auth.modelo.Usuario;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SeedAdminTest {
    private final PasswordHasher hasher = new PasswordHasher(1000);
    private Fakes.MapaFake<String, Usuario> mapa;
    private RepoEnMemoria repo;

    @BeforeEach
    void preparar() {
        mapa = new Fakes.MapaFake<>();
        repo = new RepoEnMemoria();
    }

    private boolean sembrar(Map<String, String> env) {
        return new SeedAdmin(mapa, repo, hasher, env).ejecutar();
    }

    @Test
    void creaAdminConContrasenaDelEntorno() {
        assertTrue(sembrar(Map.of("RUTASV_ADMIN_PASSWORD", "clave-admin-prueba")));
        Usuario u = mapa.obtener("admin");
        assertEquals(Rol.ADMIN, u.rol());
        assertEquals(1, repo.guardados());
        AuthServiceImpl auth = new AuthServiceImpl(mapa, repo, hasher);
        Sesion s = auth.iniciarSesion("admin", "clave-admin-prueba");
        assertEquals(Rol.ADMIN, s.rol());
        assertEquals(java.util.Optional.of(Rol.ADMIN), auth.validar(s));
    }

    @Test
    void usaNombreDelEntorno() {
        assertTrue(sembrar(Map.of("RUTASV_ADMIN_USER", "jefe.01", "RUTASV_ADMIN_PASSWORD", "clave-admin-prueba")));
        assertTrue(mapa.contiene("jefe.01"));
        assertFalse(mapa.contiene("admin"));
    }

    @Test
    void sinContrasenaNoCreaNada() {
        assertFalse(sembrar(Map.of()));
        assertEquals(0, mapa.tamano());
        assertEquals(0, repo.guardados());
    }

    @Test
    void contrasenaCortaNoCreaNada() {
        assertFalse(sembrar(Map.of("RUTASV_ADMIN_PASSWORD", "1234567")));
        assertEquals(0, mapa.tamano());
    }

    @Test
    void nombreInvalidoNoCreaNada() {
        assertFalse(sembrar(Map.of("RUTASV_ADMIN_USER", "a b", "RUTASV_ADMIN_PASSWORD", "clave-admin-prueba")));
        assertEquals(0, mapa.tamano());
    }

    @Test
    void noSobreescribeAdminExistente() {
        assertTrue(sembrar(Map.of("RUTASV_ADMIN_PASSWORD", "clave-admin-prueba")));
        Usuario original = mapa.obtener("admin");
        assertFalse(sembrar(Map.of("RUTASV_ADMIN_PASSWORD", "otra-clave-distinta")));
        assertSame(original, mapa.obtener("admin"));
        assertEquals(1, repo.guardados());
    }
}
