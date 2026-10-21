package edu.udb.rutasv.terminal;

import edu.udb.rutasv.modelo.UnidadTransporte;
import java.util.List;

public class PantallaDespacho {
    private Terminal terminal;

    public PantallaDespacho(Terminal terminal) {
        if (terminal == null) {
            throw new IllegalArgumentException("La terminal no puede ser nula");
        }
        this.terminal = terminal;
    }

    public void mostrarPantalla() {
        System.out.println("========================================");
        System.out.println("   PANTALLA DE DESPACHO - " + terminal.getNombre().toUpperCase());
        System.out.println("========================================");

        if (!terminal.hayUnidadesDisponibles()) {
            System.out.println("No hay unidades de transporte disponibles en este momento.");
        } else {
            System.out.println("Orden de despacho de unidades (Regla RN-4):");
            List<UnidadTransporte> orden = terminal.verOrdenDespacho();
            int pos = 1;
            for (UnidadTransporte u : orden) {
                System.out.println("  " + pos + ". ID: " + u.getId() + " | Tipo: " + u.getTipoRuta() + " | Salida: " + u.getHoraSalida());
                pos++;
            }
        }

        System.out.println("----------------------------------------");
        System.out.println("Pasajeros en fila (Regla RN-5): " + terminal.totalPasajerosEnFila());
        System.out.println("========================================");
    }
}
