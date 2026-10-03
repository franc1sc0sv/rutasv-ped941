package edu.udb.rutasv.model;

import java.time.LocalTime;

/** Unidad de transporte con su hora programada de salida. */
public record UnidadTransporte(String id, TipoRuta tipoRuta, LocalTime horaProgramada) {
}
