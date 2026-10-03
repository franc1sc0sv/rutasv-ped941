package edu.udb.rutasv.app.configuracion;

import edu.udb.rutasv.app.interfaces.SistemaTransporte;
import edu.udb.rutasv.auth.excepciones.CredencialesInvalidasException;
import edu.udb.rutasv.auth.modelo.Rol;
import edu.udb.rutasv.auth.modelo.Sesion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


class AplicacionTest {

    @TempDir
    Path dir;

    @Test
    void registroYLoginSobreviveReinicio() {
        Map<String, String> sinEntorno = Map.of();
        SistemaTransporte s1 = Aplicacion.crear(dir, sinEntorno);
        s1.registrar("usuario_prueba", "clave-sintetica-1");
        SistemaTransporte s2 = Aplicacion.crear(dir, sinEntorno);
        Sesion s = s2.iniciarSesion("usuario_prueba", "clave-sintetica-1");
        assertEquals(Rol.PASAJERO, s.rol());
    }

    @Test
    void adminSoloSiHayContrasenaEnEntorno() {
        SistemaTransporte sin = Aplicacion.crear(dir.resolve("a"), Map.of());
        assertThrows(CredencialesInvalidasException.class, () -> sin.iniciarSesion("admin", "clave-sintetica-9"));

        Map<String, String> env = Map.of("RUTASV_ADMIN_PASSWORD", "clave-sintetica-9");
        SistemaTransporte con = Aplicacion.crear(dir.resolve("b"), env);
        assertEquals(Rol.ADMIN, con.iniciarSesion("admin", "clave-sintetica-9").rol());
    }
}
