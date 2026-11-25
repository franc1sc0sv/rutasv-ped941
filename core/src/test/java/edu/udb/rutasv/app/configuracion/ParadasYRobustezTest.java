package edu.udb.rutasv.app.configuracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.udb.rutasv.app.interfaces.SistemaTransporte;
import edu.udb.rutasv.auth.excepciones.AccesoDenegadoException;
import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.modelo.EstadisticasGlobales;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Fase 3: edicion y activacion de paradas, casos borde de la ruta y de la terminal, sobre el
 * sistema completo armado por {@link Aplicacion}. Datos sinteticos.
 */
class ParadasYRobustezTest {

    @TempDir
    Path dir;

    private SistemaTransporte sistema;
    private Sesion admin;
    private Sesion ana;

    /**
     * Red de prueba: E01 -> E02 -> E04 (5 + 5 min) y un atajo directo E01 -> E04 de 20 min.
     * E03 queda aislada para probar "no hay ruta".
     */
    @BeforeEach
    void preparar() throws IOException {
        Files.writeString(dir.resolve("estaciones.csv"), String.join("\n",
                "codigo;nombre;zona;x;y;activa",
                "E01;Terminal Occidente;Occidente;10;50;true",
                "E02;Redondel Masferrer;Centro;30;50;true",
                "E03;Soyapangó;Oriente;90;55;true",
                "E04;Metrocentro;Centro;52;50;true"));
        Files.writeString(dir.resolve("tramos.csv"), String.join("\n",
                "origen;destino;minutos;km",
                "E01;E02;5;2.0",
                "E02;E04;5;2.0",
                "E01;E04;20;6.0"));
        sistema = Aplicacion.crear(dir, Map.of("RUTASV_ADMIN_PASSWORD", "clave-sintetica-9"));
        admin = sistema.iniciarSesion("admin", "clave-sintetica-9");
        ana = sistema.registrar("ana_01", "clave-sintetica-1");
    }

    private Estacion estacion(String codigo) {
        for (Estacion e : sistema.verRed(admin).estaciones()) {
            if (e.codigo().equals(codigo)) {
                return e;
            }
        }
        return null;
    }

    // ---- Editar y activar/desactivar (RN-1) ----

    @Test
    void desactivarUnaParadaLaSacaDelAutocompletadoYDijkstraLaRodea() {
        assertEquals(10, sistema.calcularRuta(ana, "E01", "E04").orElseThrow().minutos());

        sistema.activarDesactivarEstacion(admin, "E02", false);

        assertFalse(estacion("E02").activa());
        assertTrue(sistema.sugerirEstaciones(ana, "redondel").estaVacia());
        RutaCalculada rodeo = sistema.calcularRuta(ana, "E01", "E04").orElseThrow();
        assertEquals(20, rodeo.minutos(), "sin E02 solo queda el tramo directo");

        sistema.activarDesactivarEstacion(admin, "e02", true);
        assertEquals(1, sistema.sugerirEstaciones(ana, "redondel").tamano());
        assertEquals(10, sistema.calcularRuta(ana, "E01", "E04").orElseThrow().minutos());
    }

    @Test
    void editarCambiaElNombreEnElDirectorioLaRedYElAutocompletado() {
        sistema.editarEstacion(admin, new Estacion("E04", "Metrocentro Sur", "Centro", 52, 48, true));

        assertEquals("Metrocentro Sur", estacion("E04").nombre());
        assertEquals(48, estacion("E04").y());
        assertEquals("Metrocentro Sur", sistema.sugerirEstaciones(ana, "metro").obtener(0));
        assertEquals(1, sistema.sugerirEstaciones(ana, "metro").tamano());
        assertEquals("E04", sistema.buscarEstacion(ana, "metrocentro sur").orElseThrow().codigo());
        assertTrue(sistema.buscarEstacion(ana, "Metrocentro").isEmpty());
    }

    @Test
    void editarNoPermiteRobarElNombreDeOtraEstacion() {
        assertThrows(ValidacionException.class,
                () -> sistema.editarEstacion(admin, new Estacion("E04", "Terminal Occidente", "Centro", 1, 1, true)));
        assertThrows(ValidacionException.class,
                () -> sistema.editarEstacion(admin, new Estacion("E99", "Nueva", "Centro", 1, 1, true)));
    }

    @Test
    void soloElAdminEditaOActiva() {
        assertThrows(AccesoDenegadoException.class,
                () -> sistema.editarEstacion(ana, new Estacion("E04", "X", "Centro", 1, 1, true)));
        assertThrows(AccesoDenegadoException.class, () -> sistema.activarDesactivarEstacion(ana, "E04", false));
    }

    @Test
    void nombresQueSoloDifierenEnTildesSonElMismo() {
        assertThrows(ValidacionException.class,
                () -> sistema.crearEstacion(admin, new Estacion("E10", "Soyapango", "Oriente", 1, 1, true)));
        assertEquals("E03", sistema.buscarEstacion(ana, "SOYAPANGO").orElseThrow().codigo());
    }

    @Test
    void codigosYNombresQueRompenLosArchivosSeRechazan() {
        for (Estacion mala : new Estacion[] {
                new Estacion("E>16", "Nueva", "Z", 1, 1, true),
                new Estacion("E 16", "Nueva", "Z", 1, 1, true),
                new Estacion("E16", "Nue;va", "Z", 1, 1, true),
                new Estacion("E16", "Nueva", "Z", Double.NaN, 1, true),
                new Estacion("E16", "Nueva", "Z", 1, 1e308, true)}) {
            assertThrows(ValidacionException.class, () -> sistema.crearEstacion(admin, mala), mala.toString());
        }
    }

    @Test
    void elCodigoSeGuardaEnMayusculas() {
        sistema.crearEstacion(admin, new Estacion(" e16 ", "Nueva", "Z", 1, 1, true));
        assertEquals("Nueva", estacion("E16").nombre());
    }

    // ---- Ruta: casos borde ----

    @Test
    void mismoOrigenYDestinoEsUnPedidoInvalidoYNoSeGuarda() {
        assertThrows(ValidacionException.class, () -> sistema.calcularRuta(ana, "E01", "e01"));
        assertTrue(sistema.miHistorial(ana).estaVacia());
    }

    @Test
    void codigosEnMinusculasYConEspaciosFuncionan() {
        assertTrue(sistema.calcularRuta(ana, " e01 ", "e04").isPresent());
    }

    @Test
    void origenInactivoDaUnMensajeClaro() {
        sistema.activarDesactivarEstacion(admin, "E01", false);
        ValidacionException e = assertThrows(ValidacionException.class, () -> sistema.calcularRuta(ana, "E01", "E04"));
        assertTrue(e.getMessage().contains("inactiva"), e.getMessage());
    }

    @Test
    void sinTramosNoHayRutaYNoSeGuardaNada() {
        Optional<RutaCalculada> ruta = sistema.calcularRuta(ana, "E01", "E03");
        assertTrue(ruta.isEmpty());
        assertTrue(sistema.miHistorial(ana).estaVacia());
        assertTrue(sistema.busquedasRecientes(ana).estaVacia());
    }

    @Test
    void cadaRutaCalculadaQuedaEnLasBusquedasRecientes() {
        sistema.calcularRuta(ana, "E01", "E04");
        sistema.calcularRuta(ana, "E02", "E04");
        Lista<String> recientes = sistema.busquedasRecientes(ana);
        assertEquals("Redondel Masferrer → Metrocentro", recientes.obtener(0));
        assertEquals("Terminal Occidente → Metrocentro", recientes.obtener(1));
    }

    @Test
    void elTopCincoOrdenaPorConsultasYDesempataPorNombre() {
        for (int i = 0; i < 3; i++) {
            sistema.calcularRuta(ana, "E02", "E04");
        }
        sistema.calcularRuta(ana, "E01", "E04");
        sistema.calcularRuta(ana, "E01", "E02");
        EstadisticasGlobales e = sistema.estadisticas(admin);
        assertEquals(5, e.totalBusquedas());
        assertEquals("E02 -> E04", e.rutaMasConsultada());
        assertEquals(java.util.List.of("E02 -> E04", "E01 -> E02", "E01 -> E04"), e.top5());
    }

    // ---- Terminal: filas por tipo de ruta (RN-5) ----

    @Test
    void verFilaMuestraElOrdenSinAtenderANadie() {
        sistema.encolarPasajero(admin, TipoRuta.NORMAL, new Pasajero("P1", "Ana"));
        sistema.encolarPasajero(admin, TipoRuta.NORMAL, new Pasajero("P2", "Luis"));
        sistema.encolarPasajero(admin, TipoRuta.EXPRES, new Pasajero("P3", "Sofia"));

        Lista<Pasajero> normal = sistema.verFila(admin, TipoRuta.NORMAL);
        assertEquals(2, normal.tamano());
        assertEquals("P1", normal.obtener(0).id());
        assertEquals("P1", sistema.atenderPasajero(admin, TipoRuta.NORMAL).id());
        assertEquals(1, sistema.verFila(admin, TipoRuta.EXPRES).tamano());
        assertThrows(AccesoDenegadoException.class, () -> sistema.verFila(ana, TipoRuta.NORMAL));
    }

    @Test
    void unPasajeroRepetidoSeRechaza() {
        sistema.encolarPasajero(admin, TipoRuta.NORMAL, new Pasajero("P1", "Ana"));
        assertThrows(ValidacionException.class,
                () -> sistema.encolarPasajero(admin, TipoRuta.EXPRES, new Pasajero("P1", "Ana")));
    }
}
