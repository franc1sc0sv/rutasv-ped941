package edu.udb.rutasv.ruta.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import edu.udb.rutasv.nucleo.temporal.ColaPrioridadTemporal;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;
import edu.udb.rutasv.ruta.RedDeEjemplo;
import edu.udb.rutasv.ruta.algoritmos.Dijkstra;
import edu.udb.rutasv.ruta.estructuras.GrafoListaAdyacencia;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class RutaServiceImplTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-24T15:30:00Z"), ZoneOffset.UTC);

    private final GrafoListaAdyacencia red = RedDeEjemplo.crear();
    private final RutaServiceImpl servicio =
            new RutaServiceImpl(red, new Dijkstra(ColaPrioridadTemporal::new, MapaTemporal::new), RELOJ);

    @Test
    void armaLaRutaCalculadaCompleta() {
        RutaCalculada r = servicio.calcular("E01", "E04", "ana_01").orElseThrow();
        assertEquals("E01", r.origen());
        assertEquals("E04", r.destino());
        assertEquals(List.of("E01", "E02", "E03", "E04"), r.tramos());
        assertEquals(18, r.minutos());
        assertEquals(9.5, r.km());
        assertEquals(LocalDateTime.of(2026, 10, 24, 15, 30), r.fecha());
        assertEquals("ana_01", r.usuario());
        assertTrue(r.tiempoCalculoNs() >= 0);
    }

    @Test
    void sinRutaDevuelveVacioRn6() {
        red.agregarEstacion(new Estacion("E99", "Aislada", "Z", 0, 0, true));
        assertTrue(servicio.calcular("E01", "E99", "ana_01").isEmpty());
    }

    @Test
    void codigoInexistenteONuloLanzaValidacion() {
        assertThrows(ValidacionException.class, () -> servicio.calcular("E01", "ZZ", "ana_01"));
        assertThrows(ValidacionException.class, () -> servicio.calcular(null, "E04", "ana_01"));
        assertThrows(ValidacionException.class, () -> servicio.calcular("E01", " ", "ana_01"));
    }
}
