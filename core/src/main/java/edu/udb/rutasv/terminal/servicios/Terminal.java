package edu.udb.rutasv.terminal.servicios;

import edu.udb.rutasv.nucleo.estructuras.Cola;
import edu.udb.rutasv.nucleo.estructuras.ColaPrioridad;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.estructuras.impl.ColaEnlazada;
import edu.udb.rutasv.nucleo.estructuras.impl.ListaDinamica;
import edu.udb.rutasv.nucleo.estructuras.impl.MonticuloBinario;
import edu.udb.rutasv.nucleo.estructuras.impl.TablaHash;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;
import java.util.function.Supplier;

/**
 * Una terminal de buses (rol Terminal). Junta las dos reglas del negocio:
 *
 * <ul>
 *   <li>RN-4: las unidades salen por prioridad. Esperan en una cola de prioridad (montículo binario):
 *       primero las EXPRES, luego por hora programada y, si empatan, por id. Ver {@link ReglaDespacho}.
 *   <li>RN-5: los pasajeros se atienden en orden de llegada. Hay una fila FIFO (cola enlazada) por
 *       cada tipo de ruta, guardadas en una tabla hash {@code TipoRuta -> Cola}.
 * </ul>
 *
 * <p>Dos tablas hash evitan búsquedas lineales: {@code unidadesEnEspera} (id -> unidad) detecta una
 * unidad repetida en O(1) y permite ver el orden sin vaciar la cola real; {@code pasajerosEnFila}
 * (id -> tipo) impide que un pasajero esté dos veces en las filas.
 */
public class Terminal {

    private final String nombre;
    private final ColaPrioridad<UnidadTransporte> unidadesDisponibles;
    /** Crea una cola vacía con la misma regla que {@code unidadesDisponibles}, para ver el orden sobre una copia. */
    private final Supplier<ColaPrioridad<UnidadTransporte>> colaVacia;
    private final Mapa<String, UnidadTransporte> unidadesEnEspera = new TablaHash<>();
    private final Mapa<TipoRuta, Cola<Pasajero>> filas = new TablaHash<>();
    private final Mapa<String, TipoRuta> pasajerosEnFila = new TablaHash<>();

    /** Terminal con la regla RN-4 y las estructuras propias del equipo. */
    public Terminal(String nombre) {
        this(nombre, new MonticuloBinario<>(new ReglaDespacho()));
    }

    /** Terminal con una cola de unidades dada; {@code verOrdenDespacho} ordena con {@link ReglaDespacho}. */
    public Terminal(String nombre, ColaPrioridad<UnidadTransporte> unidadesDisponibles) {
        this(nombre, unidadesDisponibles, () -> new MonticuloBinario<>(new ReglaDespacho()));
    }

    /**
     * Terminal con una regla de despacho cualquiera. {@code colaVacia} debe crear colas vacías que
     * ordenen igual que {@code unidadesDisponibles}.
     */
    public Terminal(String nombre, ColaPrioridad<UnidadTransporte> unidadesDisponibles,
                    Supplier<ColaPrioridad<UnidadTransporte>> colaVacia) {
        if (nombre == null || nombre.isBlank()) {
            throw new ValidacionException("El nombre de la terminal no puede estar vacío");
        }
        if (unidadesDisponibles == null || colaVacia == null) {
            throw new ValidacionException("La cola de unidades de la terminal no puede ser nula");
        }
        this.nombre = nombre.trim();
        this.unidadesDisponibles = unidadesDisponibles;
        this.colaVacia = colaVacia;
        for (TipoRuta tipo : TipoRuta.values()) {
            filas.poner(tipo, new ColaEnlazada<>());
        }
    }

    public String getNombre() {
        return nombre;
    }

    // ---- Unidades (RN-4) ----

    /** Agrega una unidad a la cola de despacho. O(log n). */
    public void registrarUnidad(UnidadTransporte unidad) {
        if (unidad == null || unidad.id() == null || unidad.id().isBlank()
                || unidad.tipoRuta() == null || unidad.horaProgramada() == null) {
            throw new ValidacionException("La unidad necesita id, tipo de ruta y hora programada");
        }
        String id = unidad.id().trim();
        if (unidadesEnEspera.contiene(id)) {
            throw new ValidacionException("La unidad " + id + " ya está registrada en la terminal");
        }
        UnidadTransporte limpia = new UnidadTransporte(id, unidad.tipoRuta(), unidad.horaProgramada());
        unidadesEnEspera.poner(id, limpia);
        unidadesDisponibles.insertar(limpia);
    }

    /** Unidad que saldría ahora, sin despacharla. O(1). Lanza ColeccionVaciaException si no hay. */
    public UnidadTransporte proximaUnidadASalir() {
        return unidadesDisponibles.ver();
    }

    /** Despacha la siguiente unidad. O(log n). Lanza ColeccionVaciaException si no hay. */
    public UnidadTransporte despacharUnidad() {
        UnidadTransporte unidad = unidadesDisponibles.extraer();
        unidadesEnEspera.eliminar(unidad.id());
        return unidad;
    }

    /**
     * Unidades pendientes en el orden en que saldrían. Copia las unidades en espera a un montículo
     * nuevo y extrae de la copia: la cola real no se toca. O(n log n). Pide un comparador sin
     * empates (como {@link ReglaDespacho}) para que el orden sea siempre el mismo.
     */
    public Lista<UnidadTransporte> verOrdenDespacho() {
        ColaPrioridad<UnidadTransporte> copia = colaVacia.get();
        for (String id : unidadesEnEspera.claves()) {
            copia.insertar(unidadesEnEspera.obtener(id));
        }
        Lista<UnidadTransporte> orden = new ListaDinamica<>();
        while (!copia.estaVacia()) {
            orden.agregar(copia.extraer());
        }
        return orden;
    }

    public boolean hayUnidadesDisponibles() {
        return !unidadesDisponibles.estaVacia();
    }

    // ---- Pasajeros (RN-5) ----

    /** Pone al pasajero al final de la fila de su tipo de ruta. O(1). */
    public void encolarPasajero(TipoRuta tipo, Pasajero pasajero) {
        Cola<Pasajero> fila = fila(tipo);
        if (pasajero == null || pasajero.id() == null || pasajero.id().isBlank()
                || pasajero.nombre() == null || pasajero.nombre().isBlank()) {
            throw new ValidacionException("El pasajero necesita id y nombre");
        }
        String id = pasajero.id().trim();
        TipoRuta yaEnFila = pasajerosEnFila.obtener(id);
        if (yaEnFila != null) {
            throw new ValidacionException("El pasajero " + id + " ya está en la fila " + yaEnFila);
        }
        fila.encolar(new Pasajero(id, pasajero.nombre().trim()));
        pasajerosEnFila.poner(id, tipo);
    }

    /** Atiende al primero de la fila de ese tipo. O(1). Lanza ColeccionVaciaException si no hay nadie. */
    public Pasajero atenderPasajero(TipoRuta tipo) {
        Pasajero pasajero = fila(tipo).desencolar();
        pasajerosEnFila.eliminar(pasajero.id());
        return pasajero;
    }

    /**
     * Pasajeros de la fila en orden de llegada, sin atender a nadie. Da una vuelta completa a la
     * cola (desencolar y volver a encolar cada uno), así que la fila queda igual. O(n).
     */
    public Lista<Pasajero> verFila(TipoRuta tipo) {
        Cola<Pasajero> fila = fila(tipo);
        Lista<Pasajero> copia = new ListaDinamica<>();
        int total = fila.tamano();
        for (int i = 0; i < total; i++) {
            Pasajero pasajero = fila.desencolar();
            copia.agregar(pasajero);
            fila.encolar(pasajero);
        }
        return copia;
    }

    /** Personas esperando en la fila de ese tipo. O(1). */
    public int totalEnFila(TipoRuta tipo) {
        return fila(tipo).tamano();
    }

    private Cola<Pasajero> fila(TipoRuta tipo) {
        if (tipo == null) {
            throw new ValidacionException("El tipo de ruta es obligatorio");
        }
        return filas.obtener(tipo);
    }
}
