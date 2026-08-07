package com.uth.supereconomicorepartidor.utils;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;

import retrofit2.Response;

public final class UserFriendlyError {
    private UserFriendlyError() {}

    public static String fromThrowable(Throwable throwable) {
        if (throwable instanceof UnknownHostException) {
            return "No hay conexión a internet. Revisa tu señal e intenta nuevamente.";
        }
        if (throwable instanceof SocketTimeoutException) {
            return "El servidor tardó demasiado en responder. Intenta nuevamente.";
        }
        return fromMessage(throwable != null ? throwable.getMessage() : null);
    }

    public static String fromResponse(Response<?> response, String fallback) {
        if (response == null) return cleanFallback(fallback);
        switch (response.code()) {
            case 400:
                return "La información enviada no es válida. Revisa los datos e intenta nuevamente.";
            case 401:
                return "Tu sesión no es válida o ha vencido. Inicia sesión nuevamente.";
            case 403:
                return "No tienes permiso para realizar esta acción.";
            case 404:
                return "No encontramos la información solicitada.";
            case 409:
                return "Ya existe un registro con esos datos.";
            case 422:
                return "Hay datos incompletos o incorrectos. Revísalos e intenta nuevamente.";
            case 429:
                return "Se hicieron demasiados intentos. Espera un momento e intenta de nuevo.";
            case 500:
            case 502:
            case 503:
            case 504:
                return "El servicio no está disponible en este momento. Intenta más tarde.";
            default:
                return cleanFallback(fallback);
        }
    }

    public static String fromMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "No se pudo completar la acción. Intenta nuevamente.";
        }
        String lower = message.toLowerCase(Locale.US);
        if (lower.contains("404") || lower.contains("not found") || lower.contains("pgrst202")) {
            return "No encontramos la información solicitada. Intenta actualizar la app o vuelve a intentarlo.";
        }
        if (lower.contains("401") || lower.contains("jwt") || lower.contains("unauthorized") || lower.contains("sesion")) {
            return "Tu sesión venció. Inicia sesión nuevamente.";
        }
        if (lower.contains("403") || lower.contains("forbidden") || lower.contains("denied")) {
            return "No tienes permiso para realizar esta acción.";
        }
        if (lower.contains("network") || lower.contains("failed to connect") || lower.contains("timeout")
                || lower.contains("unable to resolve host")) {
            return "No se pudo conectar con el servidor. Revisa tu internet e intenta nuevamente.";
        }
        if (lower.contains("email not confirmed") || lower.contains("confirm")) {
            return "Debes confirmar tu correo electrónico antes de iniciar sesión.";
        }
        if (lower.contains("invalid login") || lower.contains("invalid credentials")
                || lower.contains("incorrectos")) {
            return "Correo, teléfono o contraseña incorrectos.";
        }
        if (lower.contains("already registered") || lower.contains("already exists") || lower.contains("duplicate")) {
            return "Ya existe una cuenta con esos datos.";
        }
        if (lower.contains("same_password")) {
            return "La nueva contraseña debe ser diferente a la anterior.";
        }
        if (lower.contains("weak_password") || lower.contains("password should")) {
            return "La contraseña no cumple los requisitos de seguridad.";
        }
        if (lower.contains("expired") || lower.contains("invalid")) {
            return "El enlace expiró o no es válido. Solicita uno nuevo.";
        }
        if (lower.contains("{") || lower.contains("code:") || lower.contains("codigo")
                || lower.contains("exception") || lower.contains("http")) {
            return cleanFallback(null);
        }
        return message;
    }

    private static String cleanFallback(String fallback) {
        if (fallback != null && !fallback.trim().isEmpty()) {
            return fallback;
        }
        return "No se pudo completar la acción. Intenta nuevamente.";
    }
}
