package edu.udb.rutasv.terminal.servicios;

import edu.udb.rutasv.nucleo.estructuras.Cola;
import edu.udb.rutasv.nucleo.estructuras.ColaPrioridad;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;

/**
 * Una terminal: sus unidades esperan en una cola de prioridad (RN-4) y sus pasajeros en una fila
 * FIFO (RN-5). Las dos colas llegan por el constructor, como interfaces de {@code nucleo}, para que
 * la terminal no dependa de una implementación concreta.
 */
public class Terminal {
    private final String nombre;
    private final ColaPrioridad<UnidadTransporte> unidadesDisponibles;
    private final Cola<Pasajero> filaPasajeros;

    public Terminal(
            String nombre, ColaPrioridad<UnidadTransporte> unidadesDisponibles, Cola<Pasajero> filaPasajeros) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la terminal no puede estar vacio");
        }
        if (unidadesDisponibles == null || filaPasajeros == null) {
            throw new IllegalArgumentException("Las colas de la terminal no pueden ser nulas");
        }
        this.nombre = nombre.trim();
        this.unidadesDisponibles = unidadesDisponibles;
        this.filaPasajeros = filaPasajeros;
    }

    public String getNombre() {
        return nombre;
    }

    public void registrarUnidad(UnidadTransporte unidad) {
        if (unidad == null) {
            throw new IllegalArgumentException("La unidad no puede ser nula");
        }
        unidadesDisponibles.insertar(unidad);
    }

    /** Unidad que saldría ahora, sin despacharla. Lanza ColeccionVaciaException si no hay. */
    public UnidadTransporte proximaUnidadASalir() {
        return unidadesDisponibles.ver();
    }

    /** Despacha la siguiente unidad. Lanza ColeccionVaciaException si no hay. */
    public UnidadTransporte despacharUnidad() {
        return unidadesDisponibles.extraer();
    }

    public boolean hayUnidadesDisponibles() {
        return !unidadesDisponibles.estaVacia();
    }

    /** Regla RN-5: la fila de pasajeros de la terminal es FIFO. */
    public void registrarPasajeroFila(Pasajero pasajero) {
        if (pasajero == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo");
        }
        filaPasajeros.encolar(pasajero);
    }

    /** Atiende al primero de la fila. Lanza ColeccionVaciaException si no hay nadie. */
    public Pasajero atenderSiguientePasajero() {
        return filaPasajeros.desencolar();
    }

    public boolean hayPasajerosEnFila() {
        return !filaPasajeros.estaVacia();
    }

    public int totalPasajerosEnFila() {
        return filaPasajeros.tamano();
    }
}
