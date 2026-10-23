package edu.udb.rutasv.ruta;

import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.ruta.estructuras.GrafoListaAdyacencia;

/** La red de data/*.csv (Fig. 8 del documento), escrita a mano para que las pruebas no lean archivos. */
public final class RedDeEjemplo {

    private RedDeEjemplo() {
    }

    public static GrafoListaAdyacencia crear() {
        GrafoListaAdyacencia red = new GrafoListaAdyacencia();
        String[][] estaciones = {
            {"E01", "Terminal Occidente"}, {"E02", "Redondel Masferrer"}, {"E03", "Plaza Merliot"},
            {"E04", "Metrocentro"}, {"E05", "Terminal Oriente"}, {"E06", "Parque Central"},
            {"E07", "Hospital Norte"}, {"E08", "Universidad Sur"}
        };
        for (String[] e : estaciones) {
            red.agregarEstacion(new Estacion(e[0], e[1], "Z", 0, 0, true));
        }
        doble(red, "E01", "E02", 8, 4.0);
        doble(red, "E02", "E03", 8, 4.5);
        doble(red, "E03", "E04", 2, 1.0);
        doble(red, "E02", "E06", 5, 2.0);
        doble(red, "E06", "E07", 10, 5.5);
        doble(red, "E04", "E05", 15, 9.0);
        doble(red, "E03", "E08", 9, 4.8);
        return red;
    }

    private static void doble(GrafoListaAdyacencia red, String a, String b, int minutos, double km) {
        red.agregarTramo(a, b, minutos, km);
        red.agregarTramo(b, a, minutos, km);
    }
}
