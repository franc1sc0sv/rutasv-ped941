package edu.udb.rutasv.auth.servicios;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hash de contrasenas con PBKDF2WithHmacSHA256, sal de 16 bytes y clave de 256 bits. */
public class PasswordHasher {

    /** Iteraciones por defecto. */
    public static final int ITERACIONES_POR_DEFECTO = 210_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_CLAVE = 256;

    private final int iteraciones;
    private final SecureRandom azar = new SecureRandom();

    public PasswordHasher() {
        this(ITERACIONES_POR_DEFECTO);
    }

    /** Las pruebas usan pocas iteraciones para ir rapido. */
    public PasswordHasher(int iteraciones) {
        if (iteraciones < 1) {
            throw new IllegalArgumentException("iteraciones debe ser mayor que 0");
        }
        this.iteraciones = iteraciones;
    }

    /** Sal nueva en Base64. */
    public String nuevaSalBase64() {
        byte[] sal = new byte[BYTES_SAL];
        azar.nextBytes(sal);
        return Base64.getEncoder().encodeToString(sal);
    }

    /** Hash en Base64 de la contrasena con la sal dada. */
    public String hashear(String contrasena, String salBase64) {
        return Base64.getEncoder().encodeToString(derivar(contrasena, Base64.getDecoder().decode(salBase64)));
    }

    /** Compara en tiempo constante. */
    public boolean verificar(String contrasena, String salBase64, String hashBase64) {
        byte[] esperado;
        byte[] sal;
        try {
            esperado = Base64.getDecoder().decode(hashBase64);
            sal = Base64.getDecoder().decode(salBase64);
        } catch (IllegalArgumentException | NullPointerException e) {
            return false; // datos corruptos: igual que contrasena incorrecta
        }
        byte[] calculado = derivar(contrasena, sal);
        return MessageDigest.isEqual(esperado, calculado);
    }

    private byte[] derivar(String contrasena, byte[] sal) {
        char[] chars = contrasena.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, sal, iteraciones, BITS_CLAVE);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 no disponible", e);
        } finally {
            spec.clearPassword();
            java.util.Arrays.fill(chars, '\0');
        }
    }

    @Override
    public String toString() {
        return "PasswordHasher[iteraciones=" + iteraciones + "]";
    }
}
