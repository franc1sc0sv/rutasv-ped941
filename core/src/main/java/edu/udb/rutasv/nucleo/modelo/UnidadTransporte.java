package edu.udb.rutasv.nucleo.modelo;

import java.time.LocalTime;

/** Unidad de transporte con su hora programada de salida. */
public record UnidadTransporte(String id, TipoRuta tipoRuta, LocalTime horaProgramada) {
}
