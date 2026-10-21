package rutasv.terminal;

import rutasv.estructuras.ColaFIFO;
import rutasv.estructuras.ColaPrioridad;
import rutasv.modelo.UnidadTransporte;

import java.util.Comparator;

public class Terminal {
    private String nombre;
    private ColaPrioridad<UnidadTransporte> unidadesDisponibles;
    private ColaFIFO<String> filaPasajeros; // Regla RN-5: Fila en Terminal

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

    // Regla RN-5: Gestión de fila de pasajeros en Terminal (FIFO)
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
