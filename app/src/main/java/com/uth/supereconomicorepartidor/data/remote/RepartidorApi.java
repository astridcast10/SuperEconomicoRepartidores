package com.uth.supereconomicorepartidor.data.remote;

import com.uth.supereconomicorepartidor.data.remote.models.PedidoRepartidorDTO;
import com.google.gson.JsonObject;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface RepartidorApi {

    @GET("rest/v1/pedidos")
    Call<List<PedidoRepartidorDTO>> getPedidosAsignados(
            @Query("repartidor_id") String repartidorId, // llega como "eq.<uuid>"
            @Query("select") String select,
            @Query("order") String order
    );

    @PATCH("rest/v1/pedidos")
    Call<Void> actualizarEstadoPedido(
            @Header("Prefer") String prefer, // "return=minimal"
            @Query("id") String filtroId, // llega como "eq.<id>"
            @Body JsonObject cambios
    );

    @GET("rest/v1/perfiles")
    Call<List<com.uth.supereconomicorepartidor.data.remote.models.PerfilClienteDTO>> getPerfilCliente(
            @Query("id") String id,
            @Query("select") String select
    );

    @GET("rest/v1/direcciones")
    Call<List<com.uth.supereconomicorepartidor.data.remote.models.DireccionDTO>> getDireccionPorId(
            @Query("id") String id,
            @Query("select") String select
    );

    @GET("rest/v1/perfiles")
    Call<List<com.uth.supereconomicorepartidor.data.remote.models.PerfilClienteDTO>> getRolUsuario(
            @Query("id") String id,
            @Query("select") String select // usar "rol,nombre_completo"
    );

    @GET("rest/v1/pedido_items")
    Call<List<com.uth.supereconomicorepartidor.data.remote.models.ItemPedidoDTO>> getItemsPedido(
            @Query("pedido_id") String pedidoId, // "eq.<id>"
            @Query("select") String select
    );

    @PATCH("rest/v1/pedidos")
    Call<Void> actualizarEstadoRepartidor(
            @Header("Prefer") String prefer, // "return=minimal"
            @Query("id") String filtroId, // "eq.<id>"
            @Body JsonObject cambios
    );
}
