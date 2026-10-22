package edu.udb.rutasv.paradas.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CargadorCSVTest {

    private final CargadorCSV cargador = new CargadorCSV();

    private static String escribir(Path dir, String nombre, String contenido) throws IOException {
        return Files.writeString(dir.resolve(nombre), contenido).toString();
    }

    @Test
    void cargaEstacionesConElFormatoDeData(@TempDir Path dir) throws IOException {
        String ruta = escribir(dir, "estaciones.csv", "codigo;nombre;zona;x;y\nE01;Terminal Occidente;Occidente;10;50\nE02;Redondel Masferrer;Centro;30;50\n");
        CargadorCSV.ResultadoCargaEstaciones r = cargador.cargarEstacionesDesdeCSV(ruta);
        assertEquals(2, r.estacionesValidas.size());
        assertTrue(r.errores.isEmpty());
        assertEquals(30.0, r.estacionesValidas.get(1).x());
        assertTrue(r.estacionesValidas.get(0).activa());
    }

    @Test
    void lineaMalaSeAnotaConSuNumeroYLasDemasSeCargan(@TempDir Path dir) throws IOException {
        String ruta = escribir(dir, "e.csv", "codigo;nombre;zona;x;y\nE01;A;Z;1;2\nE02;B;Z\nE03;;Z;1;2\nE04;D;Z;uno;2\nE05;E;Z;5;6\n");
        CargadorCSV.ResultadoCargaEstaciones r = cargador.cargarEstacionesDesdeCSV(ruta);
        assertEquals(2, r.estacionesValidas.size());
        assertEquals(3, r.errores.size());
        assertTrue(r.errores.get(0).startsWith("Línea 3"));
        assertTrue(r.errores.get(1).startsWith("Línea 4"));
        assertTrue(r.errores.get(2).startsWith("Línea 5"));
    }

    @Test
    void cargaTramosYRechazaMinutosInvalidos(@TempDir Path dir) throws IOException {
        String ruta = escribir(dir, "t.csv", "origen;destino;minutos;km\nE01;E02;8;4.0\nE02;E03;0;4.5\nE03;E04;-2;1\nE04;E05;x;1\n");
        CargadorCSV.ResultadoCargaTramos r = cargador.cargarTramosDesdeCSV(ruta);
        assertEquals(1, r.tramosValidos.size());
        assertEquals(3, r.errores.size());
        assertEquals(8.0, r.tramosValidos.get(0).minutos);
    }

    @Test
    void archivoInexistenteDevuelveUnError(@TempDir Path dir) {
        CargadorCSV.ResultadoCargaEstaciones r = cargador.cargarEstacionesDesdeCSV(dir.resolve("no-existe.csv").toString());
        assertTrue(r.estacionesValidas.isEmpty());
        assertEquals(1, r.errores.size());
        assertFalse(r.errores.get(0).isEmpty());
    }

    @Test
    void cargaLosCsvDeEjemploDelRepositorio() {
        CargadorCSV.ResultadoCargaEstaciones e = cargador.cargarEstacionesDesdeCSV("../data/estaciones.csv");
        CargadorCSV.ResultadoCargaTramos t = cargador.cargarTramosDesdeCSV("../data/tramos.csv");
        assertTrue(e.errores.isEmpty(), e.errores.toString());
        assertTrue(t.errores.isEmpty(), t.errores.toString());
        assertEquals(8, e.estacionesValidas.size());
        assertEquals(14, t.tramosValidos.size());
    }
}
