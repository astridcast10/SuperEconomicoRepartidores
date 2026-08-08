package com.uth.supereconomicorepartidor.domain.repositories;

import com.uth.supereconomicorepartidor.domain.entities.PedidoRepartidor;
import java.util.List;

public interface RepartidorRepository {

    interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    void getPedidosAsignados(String repartidorId, Callback<List<PedidoRepartidor>> callback);

    void actualizarEstado(Long pedidoId, String nuevoEstado, Callback<Void> callback);

    void actualizarEstadoConRepartidor(Long pedidoId, String nuevoEstado, String repartidorId, Callback<Void> callback);

    void getItemsPedido(Long pedidoId, Callback<List<com.uth.supereconomicorepartidor.domain.entities.ItemPedido>> callback);

    void getDireccion(Long direccionId, Callback<com.uth.supereconomicorepartidor.data.remote.models.DireccionDTO> callback);

    void getPerfil(String usuarioId, Callback<com.uth.supereconomicorepartidor.domain.entities.Usuario> callback);

    void verificarRolRepartidor(String usuarioId, Callback<Boolean> callback);
}
