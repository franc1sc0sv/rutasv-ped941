package edu.udb.rutasv.nucleo.modelo;

/** Tramo dirigido hacia una estacion destino, con tiempo (minutos) y distancia (km). */
public record Arista(String destinoCodigo, int minutos, double km) {
}
