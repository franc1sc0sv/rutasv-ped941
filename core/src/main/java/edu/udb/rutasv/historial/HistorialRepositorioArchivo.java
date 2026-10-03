package edu.udb.rutasv.historial;

import edu.udb.rutasv.nucleo.archivo.ArchivoAtomico;
import edu.udb.rutasv.nucleo.error.PersistenciaException;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import edu.udb.rutasv.nucleo.temporal.ListaTemporal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Guarda el historial en texto, una ruta por linea:
 * {@code origen;destino;cod1,cod2,...;minutos;km;fecha;tiempoCalculoNs;usuario}.
 */
public class HistorialRepositorioArchivo implements HistorialRepositorio {

    private final Path archivo;
    private final Supplier<Lista<RutaCalculada>> fabrica;

    /** Usa la lista temporal por defecto. */
    public HistorialRepositorioArchivo(Path archivo) {
        this(archivo, ListaTemporal::new);
    }

    /** Permite elegir la lista que se devuelve. */
    public HistorialRepositorioArchivo(Path archivo, Supplier<Lista<RutaCalculada>> fabrica) {
        this.archivo = archivo;
        this.fabrica = fabrica;
    }

    @Override
    public synchronized Lista<RutaCalculada> cargar() {
        Lista<RutaCalculada> resultado = fabrica.get();
        List<String> lineas = ArchivoAtomico.leer(archivo);
        for (int i = 0; i < lineas.size(); i++) {
            if (!lineas.get(i).isBlank()) {
                resultado.agregar(parsear(lineas.get(i), i + 1));
            }
        }
        return resultado;
    }

    @Override
    public synchronized void agregar(RutaCalculada r) {
        String nueva = formatear(r);
        List<String> lineas = new ArrayList<>(ArchivoAtomico.leer(archivo));
        lineas.add(nueva);
        ArchivoAtomico.escribir(archivo, lineas);
    }

    private static String formatear(RutaCalculada r) {
        for (String t : r.tramos()) {
            revisar(t);
        }
        revisar(r.origen());
        revisar(r.destino());
        revisar(r.usuario());
        return r.origen() + ";" + r.destino() + ";" + String.join(",", r.tramos()) + ";" + r.minutos() + ";"
                + r.km() + ";" + r.fecha() + ";" + r.tiempoCalculoNs() + ";" + r.usuario();
    }

    private static void revisar(String campo) {
        if (campo == null || campo.contains(";") || campo.contains(",") || campo.contains("\n")
                || campo.contains("\r")) {
            throw new PersistenciaException("Campo de historial invalido (nulo o con ';', ',' o salto de linea)");
        }
    }

    private RutaCalculada parsear(String linea, int numero) {
        String[] c = linea.split(";", -1);
        if (c.length != 8) {
            throw new PersistenciaException("Linea " + numero + " mal formada en " + archivo.getFileName()
                    + ": se esperan 8 campos");
        }
        try {
            List<String> tramos = c[2].isEmpty() ? List.of() : List.copyOf(Arrays.asList(c[2].split(",")));
            return new RutaCalculada(c[0], c[1], tramos, Integer.parseInt(c[3]), Double.parseDouble(c[4]),
                    LocalDateTime.parse(c[5]), Long.parseLong(c[6]), c[7]);
        } catch (RuntimeException e) {
            throw new PersistenciaException("Linea " + numero + " mal formada en " + archivo.getFileName()
                    + ": numero o fecha invalida", e);
        }
    }
}
