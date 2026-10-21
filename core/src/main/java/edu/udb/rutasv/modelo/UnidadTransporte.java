package edu.udb.rutasv.modelo;

import java.time.LocalTime;

public class UnidadTransporte implements Comparable<UnidadTransporte> {
    private String idUnidad;
    private String ruta;
    private LocalTime horaProgramada;
    private TipoRuta tipoRuta;

    public UnidadTransporte(String idUnidad, String ruta, LocalTime horaProgramada, TipoRuta tipoRuta) {
        if (idUnidad == null || idUnidad.trim().isEmpty() || horaProgramada == null || tipoRuta == null) {
            throw new IllegalArgumentException("Datos de la unidad inválidos.");
        }
        this.idUnidad = idUnidad.trim();
        this.ruta = ruta;
        this.horaProgramada = horaProgramada;
        this.tipoRuta = tipoRuta;
    }

    public String getIdUnidad() {
        return idUnidad;
    }

    public String getRuta() {
        return ruta;
    }

    public LocalTime getHoraProgramada() {
        return horaProgramada;
    }

    public TipoRuta getTipoRuta() {
        return tipoRuta;
    }

    @Override
    public int compareTo(UnidadTransporte otra) {
        // Regla RN-4: Exprés sale antes que Normal
        if (this.tipoRuta != otra.tipoRuta) {
            return this.tipoRuta == TipoRuta.EXPRES ? -1 : 1;
        }
        // Si son del mismo tipo, sale primero la hora más próxima
        int compHora = this.horaProgramada.compareTo(otra.horaProgramada);
        if (compHora != 0) {
            return compHora;
        }
        // Desempate determinista por ID
        return this.idUnidad.compareTo(otra.idUnidad);
    }
}
