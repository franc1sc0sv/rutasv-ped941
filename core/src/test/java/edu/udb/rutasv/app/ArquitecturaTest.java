package edu.udb.rutasv.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Reglas de arquitectura. Si una falla, el mensaje dice que archivo la rompe.
 *
 * <ol>
 *   <li>El core no conoce ninguna interfaz concreta (JavaFX, HTTP, servlets).
 *   <li>{@code nucleo} no depende de ningun modulo.
 *   <li>Cada modulo depende solo de {@code nucleo} (y de si mismo).
 *   <li>Solo {@code app} conoce a todos los modulos: es la unica puerta de entrada.
 * </ol>
 */
class ArquitecturaTest {

    private static final Path MAIN = Path.of("src/main/java/edu/udb/rutasv");
    private static final Pattern IMPORT = Pattern.compile("^import (?:static )?([\\w.]+);", Pattern.MULTILINE);
    private static final Set<String> MODULOS =
            Set.of("auth", "ruta", "paradas", "busqueda", "terminal", "historial");
    private static final List<String> PROHIBIDOS =
            List.of("javafx.", "com.sun.net.httpserver", "javax.servlet", "jakarta.", "io.javalin", "org.springframework");

    private record Fuente(String modulo, String archivo, List<String> imports) {}

    private static List<Fuente> fuentes() throws IOException {
        List<Fuente> out = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(MAIN)) {
            for (Path p : paths.filter(f -> f.toString().endsWith(".java")).toList()) {
                String modulo = MAIN.relativize(p).getName(0).toString();
                String texto = Files.readString(p);
                List<String> imports = new ArrayList<>();
                Matcher m = IMPORT.matcher(texto);
                while (m.find()) imports.add(m.group(1));
                out.add(new Fuente(modulo, MAIN.relativize(p).toString(), imports));
            }
        }
        return out;
    }

    private static String moduloDe(String importacion) {
        String prefijo = "edu.udb.rutasv.";
        if (!importacion.startsWith(prefijo)) return null;
        return importacion.substring(prefijo.length()).split("\\.")[0];
    }

    @Test
    void elCoreNoDependeDeNingunaInterfaz() throws IOException {
        for (Fuente f : fuentes())
            for (String i : f.imports())
                for (String prohibido : PROHIBIDOS)
                    assertTrue(!i.startsWith(prohibido), f.archivo() + " importa " + i + ": el core no puede conocer la interfaz");
    }

    @Test
    void nucleoNoDependeDeNingunModulo() throws IOException {
        for (Fuente f : fuentes()) {
            if (!f.modulo().equals("nucleo")) continue;
            for (String i : f.imports()) {
                String m = moduloDe(i);
                assertTrue(m == null || m.equals("nucleo"), f.archivo() + " importa " + i + ": nucleo no depende de modulos");
            }
        }
    }

    @Test
    void cadaModuloDependeSoloDeNucleo() throws IOException {
        for (Fuente f : fuentes()) {
            if (!MODULOS.contains(f.modulo())) continue;
            for (String i : f.imports()) {
                String m = moduloDe(i);
                assertTrue(m == null || m.equals("nucleo") || m.equals(f.modulo()),
                        f.archivo() + " importa " + i + ": un modulo solo puede depender de nucleo; coordina por app o por un contrato en nucleo");
            }
        }
    }
}
