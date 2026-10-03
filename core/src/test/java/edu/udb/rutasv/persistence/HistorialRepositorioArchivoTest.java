package edu.udb.rutasv.persistence;

import static org.junit.jupiter.api.Assertions.*;

import edu.udb.rutasv.model.RutaCalculada;
import edu.udb.rutasv.structures.Lista;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HistorialRepositorioArchivoTest {
    @TempDir
    Path dir;
    private Path archivo;
    private HistorialRepositorioArchivo repo;

    @BeforeEach
    void preparar() {
        archivo = dir.resolve("historial.txt");
        repo = new HistorialRepositorioArchivo(archivo, ListaFake::new);
    }

    private static RutaCalculada ruta(String origen, String destino) {
        return new RutaCalculada(origen, destino, List.of(origen, "E2", destino), 25, 12.5,
                LocalDateTime.of(2025, 3, 4, 10, 30, 15), 123456L, "ana");
    }

    @Test
    void archivoInexistenteDaListaVacia() {
        assertTrue(repo.cargar().estaVacia());
    }

    @Test
    void agregarYCargarConservaOrdenYDatos() {
        repo.agregar(ruta("E1", "E3"));
        repo.agregar(ruta("E4", "E5"));
        Lista<RutaCalculada> c = repo.cargar();
        assertEquals(2, c.tamano());
        assertEquals(ruta("E1", "E3"), c.obtener(0));
        assertEquals("E5", c.obtener(1).destino());
    }

    @Test
    void sobreviveReinicio() {
        repo.agregar(ruta("E1", "E3"));
        HistorialRepositorioArchivo otro = new HistorialRepositorioArchivo(archivo, ListaFake::new);
        otro.agregar(ruta("E2", "E3"));
        assertEquals(2, otro.cargar().tamano());
    }

    @Test
    void formatoDeLinea() throws IOException {
        repo.agregar(ruta("E1", "E3"));
        assertEquals(List.of("E1;E3;E1,E2,E3;25;12.5;2025-03-04T10:30:15;123456;ana"), Files.readAllLines(archivo));
    }

    @Test
    void lineaMalFormadaFalla() throws IOException {
        Files.write(archivo, List.of("E1;E3;solo;cuatro"));
        PersistenciaException e = assertThrows(PersistenciaException.class, () -> repo.cargar());
        assertTrue(e.getMessage().contains("Linea 1"));
    }

    @Test
    void numeroInvalidoFalla() throws IOException {
        Files.write(archivo, List.of("E1;E3;E1,E3;xx;1.0;2025-03-04T10:30:15;1;ana"));
        assertThrows(PersistenciaException.class, () -> repo.cargar());
    }

    @Test
    void campoConPuntoYComaRechazado() {
        assertThrows(PersistenciaException.class, () -> repo.agregar(ruta("E;1", "E3")));
        assertFalse(Files.exists(archivo));
    }
}
