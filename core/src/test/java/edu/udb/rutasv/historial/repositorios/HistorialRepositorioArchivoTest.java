package edu.udb.rutasv.historial.repositorios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.excepciones.PersistenciaException;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HistorialRepositorioArchivoTest {

    @TempDir
    Path dir;

    private static RutaCalculada ruta(String usuario, List<String> tramos, int minutos, double km) {
        return new RutaCalculada(tramos.get(0), tramos.get(tramos.size() - 1), tramos, minutos, km,
                LocalDateTime.of(2026, 10, 22, 8, 30, 15), 125_000, usuario);
    }

    @Test
    void archivoInexistenteDaListaVacia() {
        assertTrue(new HistorialRepositorioArchivo(dir.resolve("historial.txt")).cargar().estaVacia());
    }

    @Test
    void tresRutasQuedanEnOrdenDespuesDeGuardarYCargar() {
        Path archivo = dir.resolve("historial.txt");
        List<RutaCalculada> rutas = List.of(
                ruta("ana", List.of("E01", "E02", "E04"), 18, 7.5),
                ruta("bob", List.of("E04", "E07"), 9, 3.25),
                ruta("ana", List.of("E02", "E03"), 5, 2.0));
        HistorialRepositorioArchivo repo = new HistorialRepositorioArchivo(archivo);
        for (RutaCalculada r : rutas) {
            repo.agregar(r);
        }

        Lista<RutaCalculada> cargadas = new HistorialRepositorioArchivo(archivo).cargar();
        assertEquals(3, cargadas.tamano());
        for (int i = 0; i < 3; i++) {
            assertEquals(rutas.get(i), cargadas.obtener(i));
        }
    }

    @Test
    void lineaMalFormadaLanzaConSuNumero() throws IOException {
        Path archivo = dir.resolve("historial.txt");
        String buena = "E01;E04;E01>E02>E04;18;7.5;2026-10-22T08:30:15;125000;ana";
        for (String mala : new String[] {
                "E01;E04;E01>E04;18",
                "E01;E04;E01>E04;diez;7.5;2026-10-22T08:30:15;125000;ana",
                "E01;E04;E01>E04;18;7.5;ayer;125000;ana",
                "E01;E04;E01>E04;18;7.5;2026-10-22T08:30:15;125000;",
                "E01;E04;E01>>E04;18;7.5;2026-10-22T08:30:15;125000;ana"}) {
            Files.writeString(archivo, buena + "\n" + mala + "\n");
            HistorialRepositorioArchivo repo = new HistorialRepositorioArchivo(archivo);
            PersistenciaException e = assertThrows(PersistenciaException.class, repo::cargar);
            assertTrue(e.getMessage().startsWith("Linea 2 mal formada en historial.txt"), e.getMessage());
        }
    }

    @Test
    void agregarRechazaTextoQueRompeElFormato() {
        Path archivo = dir.resolve("historial.txt");
        HistorialRepositorioArchivo repo = new HistorialRepositorioArchivo(archivo);
        for (RutaCalculada mala : new RutaCalculada[] {
                ruta("ana;bob", List.of("E01", "E04"), 9, 3.0),
                ruta("ana", List.of("E01", "E0>4"), 9, 3.0),
                ruta("ana\nbob", List.of("E01", "E04"), 9, 3.0),
                new RutaCalculada("E01", "E04", List.of(), 9, 3.0, LocalDateTime.of(2026, 10, 22, 8, 0), 1, "ana")}) {
            assertThrows(PersistenciaException.class, () -> repo.agregar(mala));
        }
        assertTrue(repo.cargar().estaVacia());
    }
}
