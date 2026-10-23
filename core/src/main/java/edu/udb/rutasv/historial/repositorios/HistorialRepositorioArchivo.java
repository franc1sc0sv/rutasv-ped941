package edu.udb.rutasv.historial.repositorios;

import edu.udb.rutasv.historial.estructuras.ListaDinamica;
import edu.udb.rutasv.nucleo.archivo.ArchivoAtomico;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.excepciones.PersistenciaException;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Guarda el historial en texto, una ruta por linea (rol Historial):
 * {@code origen;destino;E01>E02>E04;minutos;km;fechaISO;tiempoCalculoNs;usuario}.
 */
public class HistorialRepositorioArchivo implements HistorialRepositorio {

    private static final Pattern SEPARADORES = Pattern.compile("[;>\r\n]");

    private final Path archivo;

    public HistorialRepositorioArchivo(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public synchronized Lista<RutaCalculada> cargar() {
        Lista<RutaCalculada> resultado = new ListaDinamica<>();
        List<String> lineas = ArchivoAtomico.leer(archivo);
        for (int i = 0; i < lineas.size(); i++) {
            if (!lineas.get(i).isBlank()) {
                resultado.agregar(parsear(lineas.get(i), i + 1));
            }
        }
        return resultado;
    }

    @Override
    public synchronized void agregar(RutaCalculada ruta) {
        validar("origen", ruta.origen());
        validar("destino", ruta.destino());
        validar("usuario", ruta.usuario());
        if (ruta.tramos().isEmpty()) {
            throw new PersistenciaException("No se puede guardar la ruta: no tiene tramos");
        }
        for (String tramo : ruta.tramos()) {
            validar("tramo", tramo);
        }
        List<String> lineas = new ArrayList<>(ArchivoAtomico.leer(archivo));
        lineas.add(String.join(";", ruta.origen(), ruta.destino(), String.join(">", ruta.tramos()),
                String.valueOf(ruta.minutos()), String.valueOf(ruta.km()), ruta.fecha().toString(),
                String.valueOf(ruta.tiempoCalculoNs()), ruta.usuario()));
        ArchivoAtomico.escribir(archivo, lineas);
    }

    private RutaCalculada parsear(String linea, int numero) {
        String prefijo = "Linea " + numero + " mal formada en " + archivo.getFileName();
        String[] c = linea.split(";", -1);
        if (c.length != 8) {
            throw new PersistenciaException(prefijo
                    + ": se esperan 8 campos origen;destino;tramos;minutos;km;fecha;tiempoNs;usuario");
        }
        String[] tramos = c[2].split(">", -1);
        if (algunoVacio(c[0], c[1], c[7]) || algunoVacio(tramos)) {
            throw new PersistenciaException(prefijo + ": origen, destino, usuario o tramo vacio");
        }
        try {
            return new RutaCalculada(c[0], c[1], List.of(tramos), Integer.parseInt(c[3]),
                    Double.parseDouble(c[4]), LocalDateTime.parse(c[5]), Long.parseLong(c[6]), c[7]);
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new PersistenciaException(prefijo + ": numero o fecha invalidos", e);
        }
    }

    private static void validar(String nombre, String campo) {
        if (campo == null || campo.isEmpty() || SEPARADORES.matcher(campo).find()) {
            throw new PersistenciaException("No se puede guardar la ruta: " + nombre
                    + " vacio o con ';', '>' o salto de linea");
        }
    }

    private static boolean algunoVacio(String... campos) {
        for (String campo : campos) {
            if (campo.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
