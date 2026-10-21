package edu.udb.rutasv.paradas.modelo;

public class Estacion {
    private String codigo;
    private String nombre;
    private String zona;
    private boolean activa;

    public Estacion(String codigo, String nombre, String zona, boolean activa) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.zona = zona;
        this.activa = activa;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getZona() {
        return zona;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
