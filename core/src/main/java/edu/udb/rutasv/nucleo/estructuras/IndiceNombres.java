package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.modelo.Estacion;

/** Indice de estaciones por nombre, con busqueda por prefijo (ignora mayusculas y acentos). */
public interface IndiceNombres {

    /** Agrega o reemplaza la estacion bajo ese nombre. O(L) con L = largo del nombre. */
    void insertar(String nombre, Estacion estacion);

    /** Quita el nombre del indice; no hace nada si no existe. O(L). */
    void eliminar(String nombre);

    /** Busca por nombre exacto (ignora mayusculas y acentos); {@code null} si no existe. O(L). */
    Estacion buscar(String nombre);

    /** Nombres que empiezan con el texto, en orden alfabetico (ignora mayusculas y acentos). O(L + k). */
    Lista<String> conPrefijo(String texto);
}
