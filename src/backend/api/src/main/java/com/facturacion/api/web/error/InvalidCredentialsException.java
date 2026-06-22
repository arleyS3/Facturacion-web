package com.facturacion.api.web.error;

/**
 * Excepción lanzada cuando las credenciales de autenticación son inválidas
 * (usuario no encontrado o contraseña incorrecta).
 * <p>
 * El {@link GlobalExceptionHandler} la intercepta y devuelve un 401 Unauthorized
 * con el mensaje descriptivo al frontend.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
