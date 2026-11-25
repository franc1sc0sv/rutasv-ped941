/**
 * Implementaciones propias de las estructuras genericas del sistema: el {@code java.util} de RutaSV.
 *
 * <p>Viven en {@code nucleo} porque todos los modulos las usan y {@code nucleo} es lo unico de lo
 * que un modulo puede depender (ver {@code ArquitecturaTest}). Cada clase implementa una interfaz de
 * {@code nucleo.estructuras} y pasa su prueba de contrato. Autoria por rol (ver historial de git):
 *
 * <ul>
 *   <li>{@link edu.udb.rutasv.nucleo.estructuras.impl.ListaDinamica}: Historial.
 *   <li>{@link edu.udb.rutasv.nucleo.estructuras.impl.PilaEnlazada}: Ruta.
 *   <li>{@link edu.udb.rutasv.nucleo.estructuras.impl.ColaEnlazada} y
 *       {@link edu.udb.rutasv.nucleo.estructuras.impl.MonticuloBinario}: Terminal.
 *   <li>{@link edu.udb.rutasv.nucleo.estructuras.impl.TablaHash}: Paradas.
 * </ul>
 *
 * <p>Las estructuras propias de un solo dominio se quedan en su modulo: el grafo en
 * {@code ruta.estructuras} y el arbol AVL en {@code busqueda.estructuras}.
 */
package edu.udb.rutasv.nucleo.estructuras.impl;
