package edu.udb.rutasv.auth.modelo;

/** Usuario guardado: sal y hash en Base64. Nunca guarda la contrasena. */
public record Usuario(String nombre, String salBase64, String hashBase64, Rol rol) {

    /** No incluye sal ni hash. */
    @Override
    public String toString() {
        return "Usuario[nombre=" + nombre + ", rol=" + rol + "]";
    }
}
