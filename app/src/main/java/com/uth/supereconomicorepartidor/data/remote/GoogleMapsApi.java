package com.uth.supereconomicorepartidor.data.remote;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleMapsApi {

    @GET("maps/api/directions/json")
    Call<DirectionsResponse> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("key") String apiKey
    );

    class DirectionsResponse {
        @SerializedName("routes")
        public List<Route> routes;
    }

    class Route {
        @SerializedName("overview_polyline")
        public OverviewPolyline overviewPolyline;
    }

    class OverviewPolyline {
        @SerializedName("points")
        public String points;
    }
}
