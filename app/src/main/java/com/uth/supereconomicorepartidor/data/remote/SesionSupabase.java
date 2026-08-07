package com.uth.supereconomicorepartidor.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SesionSupabase {
    private static final String NOMBRE_PREFERENCIAS = "sesion_supabase";
    private static final String CLAVE_TOKEN_ACCESO = "token_acceso";
    private static final String CLAVE_TOKEN_REFRESCO = "token_refresco";
    private static final String CLAVE_EXPIRA_EN = "expira_en";
    private static final String CLAVE_ID_USUARIO = "id_usuario";

    private static SharedPreferences preferencias;
    private static String tokenAcceso;
    private static String tokenRefresco;
    private static long expiraEn;
    private static String idUsuario;

    private SesionSupabase() {
    }

    public static void inicializar(Context contexto) {
        if (preferencias == null) {
            preferencias = contexto.getApplicationContext()
                    .getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE);
            tokenAcceso = preferencias.getString(CLAVE_TOKEN_ACCESO, null);
            tokenRefresco = preferencias.getString(CLAVE_TOKEN_REFRESCO, null);
            expiraEn = preferencias.getLong(CLAVE_EXPIRA_EN, 0L);
            idUsuario = preferencias.getString(CLAVE_ID_USUARIO, null);
        }
    }

    public static void guardarSesion(String token, String refreshToken, Long expiresIn, String id) {
        tokenAcceso = token;
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            tokenRefresco = refreshToken;
        }
        if (expiresIn != null && expiresIn > 0) {
            expiraEn = System.currentTimeMillis() + (expiresIn * 1000L);
        }
        idUsuario = id;
        if (preferencias != null) {
            SharedPreferences.Editor editor = preferencias.edit()
                    .putString(CLAVE_TOKEN_ACCESO, token)
                    .putString(CLAVE_ID_USUARIO, id)
                    .putLong(CLAVE_EXPIRA_EN, expiraEn);
            if (tokenRefresco != null) {
                editor.putString(CLAVE_TOKEN_REFRESCO, tokenRefresco);
            }
            editor.apply();
        }
    }

    public static void actualizarIdUsuario(String id) {
        idUsuario = id;
        if (preferencias != null) {
            preferencias.edit().putString(CLAVE_ID_USUARIO, id).apply();
        }
    }

    public static String obtenerTokenAcceso() {
        return tokenAcceso;
    }

    public static synchronized String obtenerTokenValido() {
        if (!haySesionActiva()) {
            return null;
        }
        if (expiraEn == 0L || System.currentTimeMillis() < expiraEn - 60_000L) {
            return tokenAcceso;
        }
        return renovarSesion() ? tokenAcceso : null;
    }

    private static boolean renovarSesion() {
        if (tokenRefresco == null || tokenRefresco.trim().isEmpty()) {
            cerrarSesion();
            return false;
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL(SupabaseConfig.URL + "/auth/v1/token?grant_type=refresh_token");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15_000);
            connection.setReadTimeout(15_000);
            connection.setDoOutput(true);
            connection.setRequestProperty("apikey", SupabaseConfig.ANON_KEY);
            connection.setRequestProperty("Content-Type", "application/json");

            JsonObject request = new JsonObject();
            request.addProperty("refresh_token", tokenRefresco);
            byte[] payload = request.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(payload);
            }

            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) {
                cerrarSesion();
                return false;
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
            }
            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            guardarSesion(
                    json.get("access_token").getAsString(),
                    json.get("refresh_token").getAsString(),
                    json.get("expires_in").getAsLong(),
                    idUsuario
            );
            return true;
        } catch (Exception ignored) {
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public static String obtenerIdUsuario() {
        return idUsuario;
    }

    public static boolean haySesionActiva() {
        return tokenAcceso != null && !tokenAcceso.trim().isEmpty();
    }

    public static void cerrarSesion() {
        tokenAcceso = null;
        tokenRefresco = null;
        expiraEn = 0L;
        idUsuario = null;
        if (preferencias != null) {
            preferencias.edit().clear().apply();
        }
    }
}
