package edu.udb.rutasv.persistence;

import edu.udb.rutasv.auth.AuthServiceImpl;
import edu.udb.rutasv.auth.Rol;
import edu.udb.rutasv.auth.Usuario;
import edu.udb.rutasv.structures.Lista;
import edu.udb.rutasv.structures.temporal.ListaTemporal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;
import java.util.function.Supplier;

/** Guarda usuarios en texto: {@code usuario;salBase64;hashBase64;rol}, una linea por usuario. */
public class UsuarioRepositorioArchivo implements UsuarioRepositorio {

    private static final Pattern BASE64 = Pattern.compile("[A-Za-z0-9+/]+={0,2}");

    private final Path archivo;
    private final Supplier<Lista<Usuario>> fabrica;

    /** Usa la lista temporal por defecto. */
    public UsuarioRepositorioArchivo(Path archivo) {
        this(archivo, ListaTemporal::new);
    }

    /** Permite elegir la lista que se devuelve (por ejemplo la propia del equipo). */
    public UsuarioRepositorioArchivo(Path archivo, Supplier<Lista<Usuario>> fabrica) {
        this.archivo = archivo;
        this.fabrica = fabrica;
    }

    @Override
    public synchronized Lista<Usuario> cargar() {
        Lista<Usuario> resultado = fabrica.get();
        List<String> lineas = ArchivoAtomico.leer(archivo);
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank()) {
                continue;
            }
            resultado.agregar(parsear(linea, i + 1));
        }
        return resultado;
    }

    @Override
    public synchronized void guardar(Lista<Usuario> usuarios) {
        List<String> lineas = new ArrayList<>();
        for (Usuario u : usuarios) {
            validar(u.nombre(), u.salBase64(), u.hashBase64(), u.rol() == null ? null : u.rol().name(),
                    "No se puede guardar el usuario");
            lineas.add(u.nombre() + ";" + u.salBase64() + ";" + u.hashBase64() + ";" + u.rol().name());
        }
        ArchivoAtomico.escribir(archivo, lineas);
    }

    private Usuario parsear(String linea, int numero) {
        String[] c = linea.split(";", -1);
        if (c.length != 4 || c[0].isEmpty() || c[1].isEmpty() || c[2].isEmpty()) {
            throw new PersistenciaException("Linea " + numero + " mal formada en " + archivo.getFileName()
                    + ": se esperan 4 campos usuario;sal;hash;rol");
        }
        validar(c[0], c[1], c[2], c[3], "Linea " + numero + " mal formada en " + archivo.getFileName());
        try {
            return new Usuario(c[0], c[1], c[2], Rol.valueOf(c[3]));
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Linea " + numero + " mal formada en " + archivo.getFileName()
                    + ": rol desconocido", e);
        }
    }

    /** Rechaza campos que rompen el formato de linea o no son Base64 valido. */
    private static void validar(String nombre, String sal, String hash, String rol, String prefijo) {
        if (nombre == null || !AuthServiceImpl.PATRON_USUARIO.matcher(nombre).matches()) {
            throw new PersistenciaException(prefijo + ": nombre de usuario invalido");
        }
        if (rol == null) {
            throw new PersistenciaException(prefijo + ": rol nulo");
        }
        for (String campo : new String[] {sal, hash}) {
            if (campo == null || !BASE64.matcher(campo).matches()) {
                throw new PersistenciaException(prefijo + ": sal o hash no son Base64 valido");
            }
            try {
                Base64.getDecoder().decode(campo);
            } catch (IllegalArgumentException e) {
                throw new PersistenciaException(prefijo + ": sal o hash no son Base64 valido", e);
            }
        }
    }
}
