package edu.udb.rutasv.modelo;

import java.time.LocalTime;

public class Pasajero {
    private String idPasajero;
    private String nombre;
    private LocalTime horaLlegada;

    public Pasajero(String idPasajero, String nombre, LocalTime horaLlegada) {
        if (idPasajero == null || idPasajero.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del pasajero no puede estar vacío.");
        }
        this.idPasajero = idPasajero.trim();
        this.nombre = nombre;
        this.horaLlegada = horaLlegada;
    }

    public String getIdPasajero() {
        return idPasajero;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalTime getHoraLlegada() {
        return horaLlegada;
    }
}
