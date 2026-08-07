package com.uth.supereconomicorepartidor.domain.usecases;

import com.uth.supereconomicorepartidor.domain.entities.Usuario;
import com.uth.supereconomicorepartidor.domain.repositories.AuthRepository;

public class LoginUseCase {
    private final AuthRepository authRepository;

    public LoginUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void execute(String usuario, String password, AuthRepository.Callback<Usuario> callback) {
        if (usuario == null || usuario.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            callback.onError("Correo y contraseña son obligatorios");
            return;
        }
        authRepository.login(usuario.trim(), password, new AuthRepository.Callback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuarioLogueado) {
                authRepository.logLoginAcceptance(usuarioLogueado.getId(), usuarioLogueado.getEmail());
                callback.onSuccess(usuarioLogueado);
            }
            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
