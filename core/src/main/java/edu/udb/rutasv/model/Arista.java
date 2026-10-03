package edu.udb.rutasv.model;

/** Tramo dirigido hacia una estacion destino, con tiempo (minutos) y distancia (km). */
public record Arista(String destinoCodigo, int minutos, double km) {
}
