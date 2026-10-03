package edu.udb.rutasv.model;

/** Estacion de transporte. {@code activa} indica si esta en servicio. */
public record Estacion(String codigo, String nombre, String zona, double x, double y, boolean activa) {
}
