package edu.udb.rutasv.paradas.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DirectorioEstacionesTest {

    private DirectorioEstaciones directorio;

    @BeforeEach
    void preparar() {
        directorio = new DirectorioEstaciones();
        directorio.darDeAlta("E01", "Terminal Occidente", "Occidente", 10, 50);
    }

    @Test
    void buscaPorCodigoYPorNombreSinImportarMayusculas() {
        Estacion porCodigo = directorio.buscarPorCodigo(" E01 ");
        assertEquals("Terminal Occidente", porCodigo.nombre());
        assertEquals(porCodigo, directorio.buscarPorNombre("terminal OCCIDENTE"));
        assertEquals(1, directorio.getTotalEstaciones());
    }

    @Test
    void inexistenteOVacioDevuelveNull() {
        assertNull(directorio.buscarPorCodigo("E99"));
        assertNull(directorio.buscarPorNombre("Nada"));
        assertNull(directorio.buscarPorCodigo(null));
        assertNull(directorio.buscarPorNombre("  "));
    }

    @Test
    void codigoRepetidoSeRechaza() {
        assertThrows(ValidacionException.class, () -> directorio.darDeAlta("E01", "Otra", "Centro", 1, 1));
        assertEquals(1, directorio.getTotalEstaciones());
    }

    @Test
    void nombreRepetidoSeRechazaYNoDejaMediaAlta() {
        assertThrows(ValidacionException.class, () -> directorio.darDeAlta("E02", "terminal occidente", "Centro", 1, 1));
        assertNull(directorio.buscarPorCodigo("E02"));
    }

    @Test
    void datosVaciosSeRechazan() {
        assertThrows(ValidacionException.class, () -> directorio.darDeAlta(" ", "X", "Z", 0, 0));
        assertThrows(ValidacionException.class, () -> directorio.darDeAlta("E05", null, "Z", 0, 0));
    }

    @Test
    void nuevaEstacionEstaActivaPorDefecto() {
        assertTrue(directorio.buscarPorCodigo("E01").activa());
    }
}
