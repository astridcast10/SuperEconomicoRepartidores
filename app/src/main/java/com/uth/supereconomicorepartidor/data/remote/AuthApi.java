package com.uth.supereconomicorepartidor.data.remote;

import com.google.gson.annotations.SerializedName;
import com.uth.supereconomicorepartidor.data.remote.models.PerfilClienteDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthApi {

    @POST("functions/v1/login")
    Call<AuthResponse> login(@Body EdgeLoginRequest request);

    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> loginNative(@Body LoginRequest request);

    @POST("functions/v1/login-by-phone")
    Call<AuthResponse> loginByPhone(@Body PhoneLoginRequest request);

    @GET("functions/v1/me")
    Call<MeResponse> me();

    @POST("functions/v1/logout")
    Call<Void> logout(@Header("Authorization") String token);

    @POST("auth/v1/recover")
    Call<Void> recoverPassword(@Query("redirect_to") String redirectTo, @Body RecoverRequest request);

    @POST("rest/v1/aceptaciones_login")
    Call<Void> logLoginAcceptance(@Body LoginAcceptanceRequest request);

    class LoginAcceptanceRequest {
        @SerializedName("usuario_id")
        String usuarioId;
        String email;
        public LoginAcceptanceRequest(String usuarioId, String email) {
            this.usuarioId = usuarioId;
            this.email = email;
        }
    }

    class LoginRequest {
        String email;
        String password;
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    class EdgeLoginRequest extends LoginRequest {
        String origin;
        public EdgeLoginRequest(String email, String password) {
            super(email, password);
            this.origin = "cpanel";
        }
    }

    class PhoneLoginRequest {
        String phone;
        String password;
        String origin;
        public PhoneLoginRequest(String phone, String password) {
            this.phone = phone;
            this.password = password;
            this.origin = "app_repartidor";
        }
    }

    class RecoverRequest {
        String email;
        public RecoverRequest(String email) {
            this.email = email;
        }
    }

    class AuthResponse {
        @SerializedName("access_token")
        String accessToken;
        @SerializedName("refresh_token")
        String refreshToken;
        @SerializedName("expires_in")
        Long expiresIn;
        @SerializedName("user")
        UserResponse user;
        PerfilClienteDTO profile;
        String rol;

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public Long getExpiresIn() { return expiresIn; }
        public UserResponse getUser() { return user; }
        public PerfilClienteDTO getProfile() { return profile; }
        public String getRol() { return rol; }
    }

    class UserResponse {
        String id;
        String email;
        public String getId() { return id; }
        public String getEmail() { return email; }
    }

    class MeResponse {
        UserResponse user;
        PerfilClienteDTO profile;
        String rol;
        public UserResponse getUser() { return user; }
        public PerfilClienteDTO getProfile() { return profile; }
        public String getRol() { return rol; }
    }
}
