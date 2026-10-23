package edu.udb.rutasv.app.configuracion;

import edu.udb.rutasv.app.interfaces.SistemaTransporte;
import edu.udb.rutasv.auth.excepciones.AccesoDenegadoException;
import edu.udb.rutasv.auth.excepciones.CredencialesInvalidasException;
import edu.udb.rutasv.auth.excepciones.SesionInvalidaException;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.auth.modelo.Rol;
import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.modelo.EstadisticasGlobales;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.List;
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

    @Test
    void miHistorialCargaLasRutasDelUsuarioDesdeElArchivo() throws IOException {
        Files.writeString(dir.resolve("historial.txt"), String.join("\n",
                "E01;E04;E01>E02>E04;18;7.5;2026-10-22T08:30:15;125000;usuario_prueba",
                "E04;E05;E04>E05;9;3.0;2026-10-22T09:00:00;90000;otro_usuario"));
        SistemaTransporte sistema = Aplicacion.crear(dir, Map.of());
        Sesion s = sistema.registrar("usuario_prueba", "clave-sintetica-1");

        Lista<RutaCalculada> mias = sistema.miHistorial(s);
        assertEquals(1, mias.tamano());
        assertEquals(List.of("E01", "E02", "E04"), mias.obtener(0).tramos());
    }

    @Test
    void adminVeElTop5ConElMonticuloReal() throws IOException {
        StringBuilder historial = new StringBuilder();
        for (String destino : new String[] {"E07", "E03", "E06", "E02", "E05", "E04", "E06"}) {
            historial.append("E01;" + destino + ";E01>" + destino + ";5;1.0;2026-10-22T08:00:00;100;usuario_prueba\n");
        }
        Files.writeString(dir.resolve("historial.txt"), historial);
        SistemaTransporte sistema = Aplicacion.crear(dir, Map.of("RUTASV_ADMIN_PASSWORD", "clave-sintetica-9"));

        EstadisticasGlobales e = sistema.estadisticas(sistema.iniciarSesion("admin", "clave-sintetica-9"));
        assertEquals(7, e.totalBusquedas());
        assertEquals(List.of("E01 -> E06", "E01 -> E02", "E01 -> E03", "E01 -> E04", "E01 -> E05"), e.top5());
    }

    /** Sistema armado con los CSV de ejemplo de {@code data/} y un admin. */
    private SistemaTransporte conDatosDeEjemplo() throws IOException {
        for (String archivo : new String[] {"estaciones.csv", "tramos.csv"}) {
            Files.copy(Path.of("..", "data", archivo), dir.resolve(archivo));
        }
        return Aplicacion.crear(dir, Map.of("RUTASV_ADMIN_PASSWORD", "clave-sintetica-9"));
    }

    @Test
    void calculaLaRutaDelEjemploSobreLosCsv() throws IOException {
        SistemaTransporte sistema = conDatosDeEjemplo();
        Sesion s = sistema.registrar("usuario_prueba", "clave-sintetica-1");

        RutaCalculada r = sistema.calcularRuta(s, "E01", "E04").orElseThrow();
        assertEquals(List.of("E01", "E02", "E03", "E04"), r.tramos());
        assertEquals(18, r.minutos());
        assertEquals("usuario_prueba", r.usuario());
        assertEquals("Metrocentro", sistema.buscarEstacion(s, "metrocentro").orElseThrow().nombre());
    }

    @Test
    void adminVeLaRedYAgregaTramos() throws IOException {
        SistemaTransporte sistema = conDatosDeEjemplo();
        Sesion admin = sistema.iniciarSesion("admin", "clave-sintetica-9");
        Sesion s = sistema.registrar("usuario_prueba", "clave-sintetica-1");

        assertEquals(8, sistema.verRed(admin).estaciones().tamano());
        assertTrue(sistema.calcularRuta(s, "E07", "E05").isPresent());
        assertEquals(8, sistema.verRed(s).estaciones().tamano());
        assertThrows(AccesoDenegadoException.class, () -> sistema.verRed(s).agregarTramo("E07", "E05", 1, 1.0));
        sistema.agregarTramo(admin, "E07", "E05", 1, 1.0);
        assertEquals(1, sistema.calcularRuta(s, "E07", "E05").orElseThrow().minutos());
    }

    @Test
    void despachaPorRn4YAtiendePasajerosEnOrden() throws IOException {
        SistemaTransporte sistema = conDatosDeEjemplo();
        Sesion admin = sistema.iniciarSesion("admin", "clave-sintetica-9");

        sistema.registrarUnidad(admin, new UnidadTransporte("U-005", TipoRuta.NORMAL, LocalTime.of(6, 0)));
        sistema.registrarUnidad(admin, new UnidadTransporte("U-014", TipoRuta.EXPRES, LocalTime.of(7, 0)));
        assertEquals("U-014", sistema.verOrdenDespacho(admin).obtener(0).id());
        assertEquals("U-014", sistema.despacharSiguiente(admin).id());
        assertEquals(1, sistema.verOrdenDespacho(admin).tamano());

        sistema.encolarPasajero(admin, TipoRuta.NORMAL, new Pasajero("P1", "Ana"));
        sistema.encolarPasajero(admin, TipoRuta.NORMAL, new Pasajero("P2", "Luis"));
        assertEquals("P1", sistema.atenderPasajero(admin, TipoRuta.NORMAL).id());
    }

    @Test
    void estacionCreadaApareceEnElAutocompletadoYEnLaRed() throws IOException {
        SistemaTransporte sistema = conDatosDeEjemplo();
        Sesion admin = sistema.iniciarSesion("admin", "clave-sintetica-9");
        Sesion s = sistema.registrar("usuario_prueba", "clave-sintetica-1");

        sistema.crearEstacion(admin, new Estacion("E20", "Terminal Sur", "Sur", 50, 5, true));
        sistema.crearEstacion(admin, new Estacion("E21", "Terminal Cerrada", "Sur", 60, 5, false));

        List<String> sugeridas = new java.util.ArrayList<>();
        for (String nombre : sistema.sugerirEstaciones(s, "terminal")) {
            sugeridas.add(nombre);
        }
        assertTrue(sugeridas.contains("Terminal Sur"));
        assertTrue(!sugeridas.contains("Terminal Cerrada"), "una estacion inactiva no se sugiere");
        assertEquals(10, sistema.verRed(s).estaciones().tamano());
    }

    @Test
    void cerrarSesionInvalidaLaSesion() throws IOException {
        SistemaTransporte sistema = conDatosDeEjemplo();
        Sesion s = sistema.registrar("usuario_prueba", "clave-sintetica-1");
        sistema.cerrarSesion(s);
        assertThrows(SesionInvalidaException.class, () -> sistema.calcularRuta(s, "E01", "E04"));
        sistema.cerrarSesion(null);
    }

    @Test
    void pasajeroSinIdONombreSeRechaza() throws IOException {
        SistemaTransporte sistema = conDatosDeEjemplo();
        Sesion admin = sistema.iniciarSesion("admin", "clave-sintetica-9");
        assertThrows(ValidacionException.class,
                () -> sistema.encolarPasajero(admin, TipoRuta.NORMAL, new Pasajero(null, "Ana")));
        assertThrows(ValidacionException.class,
                () -> sistema.encolarPasajero(admin, TipoRuta.NORMAL, new Pasajero("P1", " ")));
    }
}
