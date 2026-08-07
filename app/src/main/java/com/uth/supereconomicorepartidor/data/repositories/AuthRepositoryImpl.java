package com.uth.supereconomicorepartidor.data.repositories;

import com.uth.supereconomicorepartidor.data.remote.AuthApi;
import com.uth.supereconomicorepartidor.data.remote.SesionSupabase;
import com.uth.supereconomicorepartidor.data.remote.models.PerfilClienteDTO;
import com.uth.supereconomicorepartidor.domain.entities.Usuario;
import com.uth.supereconomicorepartidor.domain.repositories.AuthRepository;
import com.uth.supereconomicorepartidor.utils.UserFriendlyError;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class AuthRepositoryImpl implements AuthRepository {

    private final AuthApi authApi;
    private Usuario currentUser;

    public AuthRepositoryImpl(AuthApi authApi) {
        this.authApi = authApi;
    }

    @Override
    public void login(String emailOrPhone, String password, Callback<Usuario> callback) {
        if (emailOrPhone == null || emailOrPhone.trim().isEmpty()) {
            callback.onError("Ingresa tu correo o teléfono registrado");
            return;
        }
        String identifier = emailOrPhone.trim();
        if (identifier.contains("@")) iniciarSesionConCorreo(identifier, password, callback);
        else iniciarSesionConTelefono(identifier, password, callback);
    }

    private void iniciarSesionConTelefono(String telefono, String password, Callback<Usuario> callback) {
        authApi.loginByPhone(new AuthApi.PhoneLoginRequest(telefono, password)).enqueue(new retrofit2.Callback<AuthApi.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthApi.AuthResponse> call, Response<AuthApi.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    AuthApi.AuthResponse auth = response.body();
                    SesionSupabase.guardarSesion(auth.getAccessToken(), auth.getRefreshToken(), auth.getExpiresIn(), auth.getUser().getId());
                    validarSesionRepartidor(callback);
                } else {
                    callback.onError(obtenerDetalleError(response, "Correo/teléfono o contraseña incorrectos"));
                }
            }
            @Override
            public void onFailure(Call<AuthApi.AuthResponse> call, Throwable t) {
                callback.onError(UserFriendlyError.fromThrowable(t));
            }
        });
    }

    private void iniciarSesionConCorreo(String email, String password, Callback<Usuario> callback) {
        authApi.login(new AuthApi.EdgeLoginRequest(email, password)).enqueue(new retrofit2.Callback<AuthApi.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthApi.AuthResponse> call, Response<AuthApi.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthApi.AuthResponse auth = response.body();
                    SesionSupabase.guardarSesion(auth.getAccessToken(), auth.getRefreshToken(), auth.getExpiresIn(), auth.getUser().getId());
                    validarSesionRepartidor(callback);
                } else if (response.code() == 404 || response.code() == 405) {
                    iniciarSesionConCorreoNativo(email, password, callback);
                } else {
                    callback.onError(obtenerDetalleError(response, "Login fallido"));
                }
            }

            @Override
            public void onFailure(Call<AuthApi.AuthResponse> call, Throwable t) {
                callback.onError(UserFriendlyError.fromThrowable(t));
            }
        });
    }

    private void iniciarSesionConCorreoNativo(String email, String password, Callback<Usuario> callback) {
        authApi.loginNative(new AuthApi.LoginRequest(email, password)).enqueue(new retrofit2.Callback<AuthApi.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthApi.AuthResponse> call, Response<AuthApi.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthApi.AuthResponse auth = response.body();
                    SesionSupabase.guardarSesion(auth.getAccessToken(), auth.getRefreshToken(), auth.getExpiresIn(), auth.getUser().getId());
                    validarSesionRepartidor(callback);
                } else {
                    callback.onError(obtenerDetalleError(response, "Login fallido"));
                }
            }

            @Override
            public void onFailure(Call<AuthApi.AuthResponse> call, Throwable t) {
                callback.onError(UserFriendlyError.fromThrowable(t));
            }
        });
    }

    @Override
    public void logLoginAcceptance(String usuarioId, String email) {
        authApi.logLoginAcceptance(new AuthApi.LoginAcceptanceRequest(usuarioId, email)).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) { }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { }
        });
    }

    // *** VALIDACIÓN CLAVE: solo deja pasar si rol == "repartidor" ***
    private void validarSesionRepartidor(Callback<Usuario> callback) {
        authApi.me().enqueue(new retrofit2.Callback<AuthApi.MeResponse>() {
            @Override
            public void onResponse(Call<AuthApi.MeResponse> call, Response<AuthApi.MeResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getProfile() != null) {
                    PerfilClienteDTO dto = response.body().getProfile();
                    if (!"repartidor".equalsIgnoreCase(dto.rol)) {
                        SesionSupabase.cerrarSesion();
                        currentUser = null;
                        if (callback != null) callback.onError("Esta cuenta no es de repartidor. Esta app es solo para repartidores.");
                        return;
                    }
                    currentUser = dto.toDomain();
                    SesionSupabase.actualizarIdUsuario(dto.id);
                    if (callback != null) callback.onSuccess(currentUser);
                } else {
                    SesionSupabase.cerrarSesion();
                    currentUser = null;
                    if (callback != null) callback.onError(UserFriendlyError.fromResponse(response, "No se pudo validar tu cuenta"));
                }
            }

            @Override
            public void onFailure(Call<AuthApi.MeResponse> call, Throwable t) {
                SesionSupabase.cerrarSesion();
                currentUser = null;
                if (callback != null) callback.onError(UserFriendlyError.fromThrowable(t));
            }
        });
    }

    private String obtenerDetalleError(Response<?> response, String mensajeBase) {
        String detalle = null;
        try {
            if (response.errorBody() != null) {
                detalle = response.errorBody().string();
            }
        } catch (IOException ignored) {
            detalle = null;
        }
        if (detalle == null || detalle.trim().isEmpty()) {
            return UserFriendlyError.fromResponse(response, mensajeBase);
        }
        String friendly = UserFriendlyError.fromMessage(detalle);
        return friendly.equals(detalle) ? UserFriendlyError.fromResponse(response, mensajeBase) : friendly;
    }

    @Override
    public void logout() {
        String accessToken = SesionSupabase.obtenerTokenAcceso();
        if (accessToken != null) {
            authApi.logout("Bearer " + accessToken).enqueue(new retrofit2.Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> response) { }
                @Override public void onFailure(Call<Void> call, Throwable t) { }
            });
        }
        SesionSupabase.cerrarSesion();
        currentUser = null;
    }

    @Override
    public Usuario getCurrentUser() {
        return currentUser;
    }
}
