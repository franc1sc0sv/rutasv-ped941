package edu.udb.rutasv.app.configuracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.udb.rutasv.app.interfaces.SistemaTransporte;
import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.nucleo.modelo.EstadisticasGlobales;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Los datos de demo cargan la red de data/ con usuarios, historial, unidades y filas. Datos sinteticos. */
class DatosDemoTest {

    private static final Map<String, String> DEMO = Map.of(
            "RUTASV_ADMIN_PASSWORD", "clave-sintetica-9",
            "RUTASV_DEMO", "1",
            "RUTASV_DEMO_PASSWORD", "clave-sintetica-1");

    @TempDir
    Path dir;

    private SistemaTransporte arrancar(Map<String, String> entorno) throws IOException {
        for (String archivo : new String[] {"estaciones.csv", "tramos.csv"}) {
            Files.copy(Path.of("..", "data", archivo), dir.resolve(archivo),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return Aplicacion.crear(dir, entorno);
    }

    @Test
    void cargaHistorialConTopCincoYTerminalLlena() throws IOException {
        SistemaTransporte sistema = arrancar(DEMO);
        Sesion admin = sistema.iniciarSesion("admin", "clave-sintetica-9");

        EstadisticasGlobales e = sistema.estadisticas(admin);
        assertEquals(30, e.totalBusquedas());
        assertEquals("E01 -> E04", e.rutaMasConsultada());
        assertEquals(List.of("E01 -> E04", "E15 -> E05", "E12 -> E09", "E20 -> E18", "E07 -> E11"), e.top5());

        assertEquals("U-014", sistema.verOrdenDespacho(admin).obtener(0).id());
        assertEquals(4, sistema.verFila(admin, TipoRuta.NORMAL).tamano());
        assertEquals(4, sistema.verFila(admin, TipoRuta.EXPRES).tamano());

        Sesion ana = sistema.iniciarSesion("ana_01", "clave-sintetica-1");
        assertEquals(11, sistema.miHistorial(ana).tamano());
        // ana calculo 4 pares distintos: las recientes no se duplican.
        assertEquals(4, sistema.busquedasRecientes(ana).tamano());
    }

    @Test
    void reiniciarNoDuplicaElHistorial() throws IOException {
        arrancar(DEMO);
        SistemaTransporte otraVez = arrancar(DEMO);
        Sesion admin = otraVez.iniciarSesion("admin", "clave-sintetica-9");
        assertEquals(30, otraVez.estadisticas(admin).totalBusquedas());
    }

    @Test
    void sinLaVariableNoCargaNada() throws IOException {
        SistemaTransporte sistema = arrancar(Map.of("RUTASV_ADMIN_PASSWORD", "clave-sintetica-9"));
        Sesion admin = sistema.iniciarSesion("admin", "clave-sintetica-9");
        assertEquals(0, sistema.estadisticas(admin).totalBusquedas());
        assertTrue(sistema.verOrdenDespacho(admin).estaVacia());
    }
}
