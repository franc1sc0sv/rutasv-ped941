package edu.udb.rutasv.terminal.servicios;

import edu.udb.rutasv.nucleo.estructuras.Cola;
import edu.udb.rutasv.nucleo.estructuras.ColaPrioridad;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;
import edu.udb.rutasv.nucleo.temporal.ListaTemporal;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;
import edu.udb.rutasv.terminal.estructuras.MonticuloBinario;
import java.util.function.Supplier;

/**
 * Una terminal: sus unidades esperan en una cola de prioridad (RN-4) y sus pasajeros en una fila
 * FIFO (RN-5). Las dos colas llegan por el constructor, como interfaces de {@code nucleo}, para que
 * la terminal no dependa de una implementación concreta.
 */
public class Terminal {
    private final String nombre;
    private final ColaPrioridad<UnidadTransporte> unidadesDisponibles;
    private final Cola<Pasajero> filaPasajeros;
    /** Crea una cola vacia con la misma regla que {@code unidadesDisponibles}, para ver el orden sobre una copia. */
    private final Supplier<ColaPrioridad<UnidadTransporte>> colaVacia;
    // TODO: reemplazar MapaTemporal por la tabla hash propia cuando llegue (rol Paradas).
    private final Mapa<String, UnidadTransporte> unidadesEnEspera = new MapaTemporal<>();

    /** Terminal con la regla RN-4: {@code verOrdenDespacho} arma su copia con {@link ReglaDespacho}. */
    public Terminal(
            String nombre, ColaPrioridad<UnidadTransporte> unidadesDisponibles, Cola<Pasajero> filaPasajeros) {
        this(nombre, unidadesDisponibles, filaPasajeros, () -> new MonticuloBinario<>(new ReglaDespacho()));
    }

    /**
     * Terminal con una regla de despacho cualquiera. {@code colaVacia} debe crear colas vacias que
     * ordenen igual que {@code unidadesDisponibles}.
     */
    public Terminal(String nombre, ColaPrioridad<UnidadTransporte> unidadesDisponibles, Cola<Pasajero> filaPasajeros,
                    Supplier<ColaPrioridad<UnidadTransporte>> colaVacia) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la terminal no puede estar vacio");
        }
        if (unidadesDisponibles == null || filaPasajeros == null || colaVacia == null) {
            throw new IllegalArgumentException("Las colas de la terminal no pueden ser nulas");
        }
        this.nombre = nombre.trim();
        this.unidadesDisponibles = unidadesDisponibles;
        this.filaPasajeros = filaPasajeros;
        this.colaVacia = colaVacia;
    }

    public String getNombre() {
        return nombre;
    }

    public void registrarUnidad(UnidadTransporte unidad) {
        if (unidad == null) {
            throw new IllegalArgumentException("La unidad no puede ser nula");
        }
        if (unidadesEnEspera.contiene(unidad.id())) {
            throw new ValidacionException("La unidad " + unidad.id() + " ya esta registrada en la terminal");
        }
        unidadesEnEspera.poner(unidad.id(), unidad);
        unidadesDisponibles.insertar(unidad);
    }

    /** Unidad que saldría ahora, sin despacharla. Lanza ColeccionVaciaException si no hay. */
    public UnidadTransporte proximaUnidadASalir() {
        return unidadesDisponibles.ver();
    }

    /** Despacha la siguiente unidad. Lanza ColeccionVaciaException si no hay. */
    public UnidadTransporte despacharUnidad() {
        UnidadTransporte unidad = unidadesDisponibles.extraer();
        unidadesEnEspera.eliminar(unidad.id());
        return unidad;
    }

    /**
     * Unidades pendientes en el orden en que saldrian. Copia las unidades en espera a un monticulo
     * nuevo y extrae de la copia: la cola real no se toca. O(n log n). Pide un comparador sin
     * empates (como {@link ReglaDespacho}) para que el orden sea siempre el mismo.
     */
    public Lista<UnidadTransporte> verOrdenDespacho() {
        ColaPrioridad<UnidadTransporte> copia = colaVacia.get();
        for (String id : unidadesEnEspera.claves()) {
            copia.insertar(unidadesEnEspera.obtener(id));
        }
        Lista<UnidadTransporte> orden = new ListaTemporal<>();
        while (!copia.estaVacia()) {
            orden.agregar(copia.extraer());
        }
        return orden;
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

    /** Pasajeros en la fila, en orden de llegada, sin atender a nadie. */
    public Lista<Pasajero> verFila() {
        Lista<Pasajero> fila = new ListaTemporal<>();
        int total = filaPasajeros.tamano();
        for (int i = 0; i < total; i++) {
            Pasajero pasajero = filaPasajeros.desencolar();
            fila.agregar(pasajero);
            filaPasajeros.encolar(pasajero);
        }
        return fila;
    }
}
