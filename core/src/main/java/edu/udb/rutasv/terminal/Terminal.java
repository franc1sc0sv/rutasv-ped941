package edu.udb.rutasv.terminal;

import edu.udb.rutasv.estructuras.ColaFIFO;
import edu.udb.rutasv.estructuras.ColaPrioridad;
import edu.udb.rutasv.paradas.estructuras.TablaHash;
import edu.udb.rutasv.modelo.Pasajero;
import edu.udb.rutasv.modelo.UnidadTransporte;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Terminal {
    private String nombreTerminal;
    private ColaPrioridad<UnidadTransporte> colaDespacho;
    private ColaFIFO<Pasajero> filaPasajeros;
    private TablaHash<String, UnidadTransporte> registroUnidadesEnEspera;

    public Terminal(String nombreTerminal) {
        this.nombreTerminal = nombreTerminal;
        this.colaDespacho = new ColaPrioridad<UnidadTransporte>(Comparator.naturalOrder());
        this.filaPasajeros = new ColaFIFO<>();
        this.registroUnidadesEnEspera = new TablaHash<>();
    }

    public void registrarUnidad(UnidadTransporte unidad) {
        if (unidad == null) {
            throw new IllegalArgumentException("La unidad no puede ser nula.");
        }
        if (registroUnidadesEnEspera.contiene(unidad.getIdUnidad())) {
            throw new IllegalArgumentException("La unidad " + unidad.getIdUnidad() + " ya está registrada en la terminal.");
        }
        registroUnidadesEnEspera.poner(unidad.getIdUnidad(), unidad);
        colaDespacho.insertar(unidad);
    }

    public UnidadTransporte despacharUnidad() {
        UnidadTransporte unidad = colaDespacho.sacarMayor();
        if (unidad != null) {
            registroUnidadesEnEspera.eliminar(unidad.getIdUnidad());
        }
        return unidad;
    }

    public UnidadTransporte proximaUnidad() {
        return colaDespacho.verMayor();
    }

    public int unidadesEnEspera() {
        return colaDespacho.tamanio();
    }

    public List<UnidadTransporte> verOrdenDespacho() {
        List<UnidadTransporte> lista = new ArrayList<>();
        ColaPrioridad<UnidadTransporte> copia = colaDespacho.copia();
        while (!copia.estaVacia()) {
            lista.add(copia.sacarMayor());
        }
        return lista;
    }

    public void encolarPasajero(Pasajero pasajero) {
        if (pasajero == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo.");
        }
        filaPasajeros.encolar(pasajero);
    }

    public Pasajero atenderPasajero() {
        return filaPasajeros.desencolar();
    }

    public Pasajero siguientePasajero() {
        return filaPasajeros.verFrente();
    }

    public int pasajerosEnEspera() {
        return filaPasajeros.tamanio();
    }

    public List<Pasajero> verFila() {
        return filaPasajeros.aLista();
    }
}
