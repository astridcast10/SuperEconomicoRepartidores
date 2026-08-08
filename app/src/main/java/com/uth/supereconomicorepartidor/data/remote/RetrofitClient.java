package com.uth.supereconomicorepartidor.data.remote;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message ->
                    android.util.Log.d("HTTP", message));
            logging.redactHeader("Authorization");
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                
                // Determinar qué token usar (lógica espejo del apiFetch en JS)
                String token = SupabaseConfig.ANON_KEY;
                if (SesionSupabase.haySesionActiva()) {
                    String userToken = SesionSupabase.obtenerTokenValido();
                    if (userToken != null && !userToken.isEmpty()) {
                        token = userToken;
                    }
                }

                Request.Builder requestBuilder = original.newBuilder()
                        .header("apikey", SupabaseConfig.ANON_KEY)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .method(original.method(), original.body());

                return chain.proceed(requestBuilder.build());
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(authInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(SupabaseConfig.URL + "/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
