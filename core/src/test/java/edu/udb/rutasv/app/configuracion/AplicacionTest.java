package edu.udb.rutasv.app.configuracion;

import edu.udb.rutasv.app.interfaces.SistemaTransporte;
import edu.udb.rutasv.auth.excepciones.CredencialesInvalidasException;
import edu.udb.rutasv.auth.modelo.Rol;
import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
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

    @Test
    void sugiereEstacionesDelCsvYRecuerdaLaBusqueda() throws IOException {
        Files.writeString(dir.resolve("estaciones.csv"), String.join("\n",
                "codigo;nombre;zona;x;y",
                "E01;Terminal Occidente;Occidente;10;50",
                "E04;Metrocentro;Centro;52;50",
                "E05;Terminal Oriente;Oriente;90;55",
                "E09;Terminal Sur;Sur;50;10;false"));
        SistemaTransporte sistema = Aplicacion.crear(dir, Map.of());
        Sesion s = sistema.registrar("usuario_prueba", "clave-sintetica-1");

        Lista<String> sugeridas = sistema.sugerirEstaciones(s, "ter");
        assertEquals(2, sugeridas.tamano());
        assertEquals("Terminal Occidente", sugeridas.obtener(0));
        assertEquals("Terminal Oriente", sugeridas.obtener(1));
        assertEquals("ter", sistema.busquedasRecientes(s).obtener(0));
    }
}
