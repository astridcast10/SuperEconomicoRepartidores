package com.uth.supereconomicorepartidor.data.remote;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OsrmApi {
    @GET("route/v1/driving/{coordinates}")
    Call<OsrmResponse> getRoute(
            @Path("coordinates") String coordinates, // lon1,lat1;lon2,lat2
            @Query("overview") String overview,      // full
            @Query("geometries") String geometries   // geojson
    );

    class OsrmResponse {
        @SerializedName("routes")
        public List<Route> routes;
    }

    class Route {
        @SerializedName("geometry")
        public Geometry geometry;
    }

    class Geometry {
        @SerializedName("coordinates")
        public List<List<Double>> coordinates; // [[lon, lat], ...]
    }
}
