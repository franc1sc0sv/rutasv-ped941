package edu.udb.rutasv.nucleo.modelo;

/** Estacion de transporte. {@code activa} indica si esta en servicio. */
public record Estacion(String codigo, String nombre, String zona, double x, double y, boolean activa) {
}
