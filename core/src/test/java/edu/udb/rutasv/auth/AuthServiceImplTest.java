package edu.udb.rutasv.auth;

import static org.junit.jupiter.api.Assertions.*;

import edu.udb.rutasv.model.ValidacionException;
import edu.udb.rutasv.persistence.UsuarioRepositorioArchivo;
import edu.udb.rutasv.structures.Lista;
import edu.udb.rutasv.structures.Mapa;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AuthServiceImplTest {
    private static final String CLAVE = "clave-de-prueba-1";
    private final PasswordHasher hasher = new PasswordHasher(1000);
    private Mapa<String, Usuario> mapa;
    private RepoEnMemoria repo;
    private AuthServiceImpl auth;

    @BeforeEach
    void preparar() {
        mapa = new Fakes.MapaFake<>();
        repo = new RepoEnMemoria();
        auth = new AuthServiceImpl(mapa, repo, hasher);
    }

    @Test
    void registrarCreaPasajero() {
        Sesion s = auth.registrar("ana_01", CLAVE);
        assertEquals("ana_01", s.usuario());
        assertEquals(Rol.PASAJERO, s.rol());
        assertTrue(mapa.contiene("ana_01"));
        assertEquals(1, repo.guardados());
    }

    @Test
    void loginCorrecto() {
        auth.registrar("ana_01", CLAVE);
        Sesion s = auth.iniciarSesion("ana_01", CLAVE);
        assertEquals("ana_01", s.usuario());
        assertEquals(Rol.PASAJERO, s.rol());
        assertEquals(Optional.of(Rol.PASAJERO), auth.validar(s));
    }

    @Test
    void contrasenaIncorrectaYUsuarioDesconocidoDanMismoError() {
        auth.registrar("ana_01", CLAVE);
        CredencialesInvalidasException a =
                assertThrows(CredencialesInvalidasException.class, () -> auth.iniciarSesion("ana_01", "otra-clave-xx"));
        CredencialesInvalidasException b =
                assertThrows(CredencialesInvalidasException.class, () -> auth.iniciarSesion("nadie", CLAVE));
        assertEquals(a.getMessage(), b.getMessage());
    }

    @Test
    void loginConNulosFalla() {
        assertThrows(CredencialesInvalidasException.class, () -> auth.iniciarSesion(null, null));
    }

    @Test
    void usuarioDuplicadoRechazado() {
        auth.registrar("ana_01", CLAVE);
        assertThrows(UsuarioExistenteException.class, () -> auth.registrar("ana_01", "otra-clave-xx"));
        assertEquals(1, repo.guardados());
    }

    @Test
    void usuarioInvalidoRechazado() {
        for (String malo : new String[] {null, "", "ab", "a".repeat(33), "con espacio", "ñandu", "a;b;c", "x\ny12"}) {
            assertThrows(ValidacionException.class, () -> auth.registrar(malo, CLAVE), String.valueOf(malo));
        }
        assertEquals(0, mapa.tamano());
    }

    @Test
    void contrasenaCortaORechazada() {
        assertThrows(ValidacionException.class, () -> auth.registrar("ana_01", "corta12"));
        assertThrows(ValidacionException.class, () -> auth.registrar("ana_01", null));
    }

    @Test
    void usuarioLimiteValido() {
        assertDoesNotThrow(() -> auth.registrar("abc", "12345678"));
        assertDoesNotThrow(() -> auth.registrar("a.b-c_" + "d".repeat(26), "12345678"));
    }

    @Test
    void registrarNuncaCreaAdmin() {
        assertEquals(Rol.PASAJERO, auth.registrar("admin", CLAVE).rol());
        assertEquals(Rol.PASAJERO, mapa.obtener("admin").rol());
    }

    @Test
    void noGuardaContrasenaEnClaro() {
        auth.registrar("ana_01", CLAVE);
        Usuario u = mapa.obtener("ana_01");
        assertFalse(u.hashBase64().contains(CLAVE));
        assertFalse(u.toString().contains(CLAVE));
        assertFalse(u.toString().contains(u.hashBase64()));
    }

    @Test
    void mensajesNoIncluyenContrasena() {
        auth.registrar("ana_01", CLAVE);
        Exception e = assertThrows(CredencialesInvalidasException.class, () -> auth.iniciarSesion("ana_01", "mala-clave-77"));
        assertFalse(e.getMessage().contains("mala-clave-77"));
    }

    @Test
    void persistenciaSobreviveReinicio(@TempDir Path dir) {
        Path archivo = dir.resolve("usuarios.txt");
        UsuarioRepositorioArchivo r1 = new UsuarioRepositorioArchivo(archivo, Fakes.ListaFake::new);
        new AuthServiceImpl(new Fakes.MapaFake<>(), r1, hasher).registrar("ana_01", CLAVE);

        // "reinicio": nuevo mapa cargado desde el archivo
        UsuarioRepositorioArchivo r2 = new UsuarioRepositorioArchivo(archivo, Fakes.ListaFake::new);
        Mapa<String, Usuario> nuevo = new Fakes.MapaFake<>();
        Lista<Usuario> cargados = r2.cargar();
        cargados.forEach(u -> nuevo.poner(u.nombre(), u));
        AuthServiceImpl auth2 = new AuthServiceImpl(nuevo, r2, hasher);

        assertEquals(Rol.PASAJERO, auth2.iniciarSesion("ana_01", CLAVE).rol());
        assertThrows(UsuarioExistenteException.class, () -> auth2.registrar("ana_01", CLAVE));
        auth2.registrar("beto_02", CLAVE);
        assertEquals(2, r2.cargar().tamano());
    }

    @Test
    void sesionDeOtroServicioNoValida() {
        auth.registrar("ana_01", CLAVE);
        AuthServiceImpl otro = new AuthServiceImpl(mapa, repo, hasher);
        assertEquals(Optional.empty(), otro.validar(auth.iniciarSesion("ana_01", CLAVE)));
        assertEquals(Optional.empty(), auth.validar(null));
    }

    @Test
    void cerrarSesionLaInvalida() {
        Sesion s = auth.registrar("ana_01", CLAVE);
        assertTrue(auth.validar(s).isPresent());
        auth.cerrarSesion(s);
        assertTrue(auth.validar(s).isEmpty());
        assertDoesNotThrow(() -> auth.cerrarSesion(null));
    }

    @Test
    void rolSeLeeDelMapaNoDeLaSesion() {
        Sesion s = auth.registrar("ana_01", CLAVE);
        Usuario u = mapa.obtener("ana_01");
        mapa.poner("ana_01", new Usuario(u.nombre(), u.salBase64(), u.hashBase64(), Rol.ADMIN));
        assertEquals(Optional.of(Rol.ADMIN), auth.validar(s));
        mapa.eliminar("ana_01");
        assertTrue(auth.validar(s).isEmpty());
    }

    @Test
    void hashCorruptoDaCredencialesInvalidas() {
        mapa.poner("rota", new Usuario("rota", "***", "###", Rol.PASAJERO));
        assertThrows(CredencialesInvalidasException.class, () -> auth.iniciarSesion("rota", CLAVE));
    }
}
