package edu.udb.rutasv.persistence;

import static org.junit.jupiter.api.Assertions.*;

import edu.udb.rutasv.auth.Rol;
import edu.udb.rutasv.auth.Usuario;
import edu.udb.rutasv.structures.Lista;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UsuarioRepositorioArchivoTest {
    @TempDir
    Path dir;
    private Path archivo;
    private UsuarioRepositorioArchivo repo;

    @BeforeEach
    void preparar() {
        archivo = dir.resolve("datos").resolve("usuarios.txt");
        repo = new UsuarioRepositorioArchivo(archivo, ListaFake::new);
    }

    @Test
    void archivoInexistenteDaListaVacia() {
        assertTrue(repo.cargar().estaVacia());
    }

    @Test
    void guardarYCargar() {
        Lista<Usuario> l = new ListaFake<>();
        l.agregar(new Usuario("ana", "c2Fs", "aGFzaA==", Rol.PASAJERO));
        l.agregar(new Usuario("root", "c2Fs", "aGFzaDI=", Rol.ADMIN));
        repo.guardar(l);
        Lista<Usuario> c = repo.cargar();
        assertEquals(2, c.tamano());
        assertEquals(new Usuario("root", "c2Fs", "aGFzaDI=", Rol.ADMIN), c.obtener(1));
    }

    @Test
    void formatoDeLinea() throws IOException {
        Lista<Usuario> l = new ListaFake<>();
        l.agregar(new Usuario("ana", "c2Fs", "aGFzaA==", Rol.PASAJERO));
        repo.guardar(l);
        assertEquals(List.of("ana;c2Fs;aGFzaA==;PASAJERO"), Files.readAllLines(archivo));
    }

    @Test
    void guardarReemplazaYNoDejaTemporales() throws IOException {
        Lista<Usuario> l = new ListaFake<>();
        l.agregar(new Usuario("ana", "c2Fs", "aGFzaA==", Rol.PASAJERO));
        repo.guardar(l);
        repo.guardar(new ListaFake<>());
        assertTrue(repo.cargar().estaVacia());
        try (var s = Files.list(archivo.getParent())) {
            assertEquals(1, s.count());
        }
    }

    @Test
    void lineaMalFormadaFallaConNumeroDeLinea() throws IOException {
        Files.createDirectories(archivo.getParent());
        Files.write(archivo, List.of("ana;c2Fs;aGFzaA==;PASAJERO", "rota;solo;tres"));
        PersistenciaException e = assertThrows(PersistenciaException.class, () -> repo.cargar());
        assertTrue(e.getMessage().contains("Linea 2"));
    }

    @Test
    void rolDesconocidoFalla() throws IOException {
        Files.createDirectories(archivo.getParent());
        Files.write(archivo, List.of("ana;c2Fs;aGFzaA==;SUPERUSUARIO"));
        assertThrows(PersistenciaException.class, () -> repo.cargar());
    }

    @Test
    void ignoraLineasEnBlanco() throws IOException {
        Files.createDirectories(archivo.getParent());
        Files.write(archivo, List.of("ana;c2Fs;aGFzaA==;PASAJERO", ""));
        assertEquals(1, repo.cargar().tamano());
    }

    @Test
    void nombreConPuntoYComaNoSeGuarda() {
        Lista<Usuario> l = new ListaFake<>();
        l.agregar(new Usuario("a;b", "c2Fs", "aGFzaA==", Rol.PASAJERO));
        assertThrows(PersistenciaException.class, () -> repo.guardar(l));
    }

    @Test
    void guardarRechazaInyeccionDeLinea() {
        Lista<Usuario> l = new ListaFake<>();
        l.agregar(new Usuario("a\nevil;AAAA;AAAA;ADMIN", "c2Fs", "aGFzaA==", Rol.PASAJERO));
        assertThrows(PersistenciaException.class, () -> repo.guardar(l));
        Lista<Usuario> m = new ListaFake<>();
        m.agregar(new Usuario("ana", "c2Fs\n", "aGFzaA==", Rol.PASAJERO));
        assertThrows(PersistenciaException.class, () -> repo.guardar(m));
        Lista<Usuario> n = new ListaFake<>();
        n.agregar(new Usuario("ana", "c2Fs", "aGFzaA==", null));
        assertThrows(PersistenciaException.class, () -> repo.guardar(n));
    }

    @Test
    void cargarRechazaBase64InvalidoYNombreInvalido() throws IOException {
        Files.createDirectories(archivo.getParent());
        Files.write(archivo, List.of("ana;c2Fs;no-es-base64!;PASAJERO"));
        PersistenciaException e = assertThrows(PersistenciaException.class, () -> repo.cargar());
        assertTrue(e.getMessage().contains("Linea 1"));
        Files.write(archivo, List.of("a b;c2Fs;aGFzaA==;PASAJERO"));
        assertThrows(PersistenciaException.class, () -> repo.cargar());
    }
}
