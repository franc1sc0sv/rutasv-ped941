package edu.udb.rutasv.historial.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import edu.udb.rutasv.historial.repositorios.HistorialRepositorioArchivo;
import edu.udb.rutasv.nucleo.modelo.EstadisticasGlobales;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import edu.udb.rutasv.nucleo.temporal.ColaPrioridadTemporal;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EstadisticasServiceTest {

    @TempDir
    Path dir;

    private HistorialService historial;
    private EstadisticasService estadisticas;

    @BeforeEach
    void crear() {
        historial = new HistorialService(new HistorialRepositorioArchivo(dir.resolve("historial.txt")));
        estadisticas = new EstadisticasService(historial, MapaTemporal::new, ColaPrioridadTemporal::new);
    }

    private void registrar(String origen, String destino, long tiempoNs) {
        historial.registrar(new RutaCalculada(origen, destino, List.of(origen, destino), 10, 4.0,
                LocalDateTime.of(2026, 10, 23, 9, 0), tiempoNs, "ana"));
    }

    @Test
    void historialVacioDaCerosSinDividirEntreCero() {
        EstadisticasGlobales e = estadisticas.calcular();
        assertEquals(0, e.totalBusquedas());
        assertNull(e.rutaMasConsultada());
        assertEquals(0, e.tiempoPromedioNs());
        assertEquals(List.of(), e.top5());
    }

    @Test
    void cuentaPorParYOrdenaElTop5DeMasAMenos() {
        registrar("E01", "E04", 100);
        registrar("E04", "E05", 200);
        registrar("E01", "E04", 300);
        registrar("E02", "E03", 400);
        registrar("E04", "E05", 500);
        registrar("E01", "E04", 600);

        EstadisticasGlobales e = estadisticas.calcular();
        assertEquals(6, e.totalBusquedas());
        assertEquals("E01 -> E04", e.rutaMasConsultada());
        assertEquals(350, e.tiempoPromedioNs());
        assertEquals(List.of("E01 -> E04", "E04 -> E05", "E02 -> E03"), e.top5());
    }

    @Test
    void conEmpatesSaleAlfabeticoYSoloCinco() {
        for (String destino : new String[] {"E07", "E03", "E06", "E02", "E05", "E04"}) {
            registrar("E01", destino, 100);
        }
        registrar("E01", "E06", 100);

        assertEquals(List.of("E01 -> E06", "E01 -> E02", "E01 -> E03", "E01 -> E04", "E01 -> E05"),
                estadisticas.calcular().top5());
    }
}
