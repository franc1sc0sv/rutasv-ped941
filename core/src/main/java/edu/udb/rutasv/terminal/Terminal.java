package edu.udb.rutasv.terminal;

import edu.udb.rutasv.terminal.estructuras.ColaFIFO;
import edu.udb.rutasv.terminal.estructuras.ColaPrioridad;
import edu.udb.rutasv.terminal.modelo.UnidadTransporte;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Terminal {
    private String nombre;
    private ColaPrioridad<UnidadTransporte> unidadesDisponibles;
    private ColaFIFO<String> filaPasajeros;

    public Terminal(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la terminal no puede estar vacio");
        }
        this.nombre = nombre.trim();
        this.unidadesDisponibles = new ColaPrioridad<>(Comparator.naturalOrder());
        this.filaPasajeros = new ColaFIFO<>();
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

    public UnidadTransporte proximaUnidadASalir() {
        return unidadesDisponibles.verMayor();
    }

    public UnidadTransporte despacharUnidad() {
        return unidadesDisponibles.sacarMayor();
    }

    public boolean hayUnidadesDisponibles() {
        return !unidadesDisponibles.estaVacia();
    }

    // Tarea 5: verOrdenDespacho sin vaciar la cola real
    public List<UnidadTransporte> verOrdenDespacho() {
        List<UnidadTransporte> listaOrdenada = new ArrayList<>();
        List<UnidadTransporte> extraidos = new ArrayList<>();

        while (hayUnidadesDisponibles()) {
            UnidadTransporte u = despacharUnidad();
            listaOrdenada.add(u);
            extraidos.add(u);
        }

        for (UnidadTransporte u : extraidos) {
            registrarUnidad(u);
        }

        return listaOrdenada;
    }

    public void registrarPasajeroFila(String pasajeroId) {
        if (pasajeroId == null || pasajeroId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del pasajero no puede estar vacio");
        }
        filaPasajeros.encolar(pasajeroId.trim());
    }

    public String atenderSiguientePasajero() {
        return filaPasajeros.desencolar();
    }

    public boolean hayPasajerosEnFila() {
        return !filaPasajeros.estaVacia();
    }

    public int totalPasajerosEnFila() {
        return filaPasajeros.tamano();
    }
}
