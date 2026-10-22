package edu.udb.rutasv.busqueda.servicios;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Normaliza nombres para compararlos sin importar mayusculas ni acentos. */
public final class Normalizador {

    private static final Pattern MARCAS = Pattern.compile("\\p{M}+");

    private Normalizador() {
    }

    /** Minusculas y sin acentos; conserva espacios. {@code null} da "". */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinAcentos = MARCAS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
        return sinAcentos.toLowerCase(Locale.ROOT);
    }
}
