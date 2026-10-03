package edu.udb.rutasv.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/** Utilidad interna: lee y escribe archivos de texto de forma atomica. */
final class ArchivoAtomico {

    private ArchivoAtomico() {
    }

    /** Lee las lineas; lista vacia si el archivo no existe. */
    static List<String> leer(Path archivo) {
        if (!Files.exists(archivo)) {
            return List.of();
        }
        try {
            return Files.readAllLines(archivo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer " + archivo.getFileName(), e);
        }
    }

    /** Escribe en un temporal y lo mueve sobre el destino. */
    static void escribir(Path archivo, List<String> lineas) {
        try {
            Path dir = archivo.toAbsolutePath().getParent();
            Files.createDirectories(dir);
            Path temporal = Files.createTempFile(dir, archivo.getFileName().toString(), ".tmp");
            try {
                Files.write(temporal, lineas, StandardCharsets.UTF_8);
                try {
                    Files.move(temporal, archivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporal);
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo escribir " + archivo.getFileName(), e);
        }
    }
}
