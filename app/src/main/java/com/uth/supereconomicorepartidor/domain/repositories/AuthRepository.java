package com.uth.supereconomicorepartidor.domain.repositories;

import com.uth.supereconomicorepartidor.domain.entities.Usuario;

public interface AuthRepository {
    interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    void login(String email, String password, Callback<Usuario> callback);
    void logLoginAcceptance(String usuarioId, String email);
    void logout();
    Usuario getCurrentUser();
}
