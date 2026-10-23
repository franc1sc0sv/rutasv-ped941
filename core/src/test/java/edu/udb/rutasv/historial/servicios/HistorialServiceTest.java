package edu.udb.rutasv.historial.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.udb.rutasv.historial.estructuras.ListaDinamica;
import edu.udb.rutasv.historial.repositorios.HistorialRepositorio;
import edu.udb.rutasv.historial.repositorios.HistorialRepositorioArchivo;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.excepciones.PersistenciaException;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HistorialServiceTest {

    @TempDir
    Path dir;

    private static RutaCalculada ruta(String usuario, String origen, String destino) {
        return new RutaCalculada(origen, destino, List.of(origen, destino), 10, 4.0,
                LocalDateTime.of(2026, 10, 23, 9, 0), 1000, usuario);
    }

    private HistorialService servicio() {
        return new HistorialService(new HistorialRepositorioArchivo(dir.resolve("historial.txt")));
    }

    @Test
    void deUsuarioDevuelveSoloLasSuyasEnOrden() {
        HistorialService historial = servicio();
        RutaCalculada primera = ruta("ana", "E01", "E04");
        RutaCalculada segunda = ruta("ana", "E04", "E07");
        historial.registrar(primera);
        historial.registrar(ruta("bob", "E02", "E03"));
        historial.registrar(segunda);

        Lista<RutaCalculada> deAna = historial.deUsuario("ana");
        assertEquals(2, deAna.tamano());
        assertEquals(primera, deAna.obtener(0));
        assertEquals(segunda, deAna.obtener(1));
        assertTrue(historial.deUsuario("carla").estaVacia());
    }

    @Test
    void elHistorialSobreviveUnReinicio() {
        RutaCalculada ruta = ruta("ana", "E01", "E04");
        servicio().registrar(ruta);
        assertEquals(ruta, servicio().deUsuario("ana").obtener(0));
    }

    @Test
    void rutaRechazadaPorElRepositorioNoQuedaEnMemoria() {
        HistorialService historial = servicio();
        RutaCalculada sinTramos = new RutaCalculada("E01", "E04", List.of(), 10, 4.0,
                LocalDateTime.of(2026, 10, 23, 9, 0), 1000, "ana");
        assertThrows(PersistenciaException.class, () -> historial.registrar(sinTramos));
        assertTrue(historial.deUsuario("ana").estaVacia());
    }

    @Test
    void cambiarLosTramosDespuesDeRegistrarNoAfectaElHistorial() {
        HistorialService historial = servicio();
        List<String> tramos = new ArrayList<>(List.of("E01", "E04"));
        historial.registrar(new RutaCalculada("E01", "E04", tramos, 10, 4.0,
                LocalDateTime.of(2026, 10, 23, 9, 0), 1000, "ana"));
        tramos.clear();
        assertEquals(List.of("E01", "E04"), historial.deUsuario("ana").obtener(0).tramos());
    }

    @Test
    void noModificaLaListaQueDevuelveElRepositorio() {
        Lista<RutaCalculada> delRepositorio = new ListaDinamica<>();
        HistorialService historial = new HistorialService(new HistorialRepositorio() {
            @Override
            public Lista<RutaCalculada> cargar() {
                return delRepositorio;
            }

            @Override
            public void agregar(RutaCalculada ruta) {
                delRepositorio.agregar(ruta);
            }
        });
        historial.registrar(ruta("ana", "E01", "E04"));
        assertEquals(1, historial.deUsuario("ana").tamano());
    }
}
