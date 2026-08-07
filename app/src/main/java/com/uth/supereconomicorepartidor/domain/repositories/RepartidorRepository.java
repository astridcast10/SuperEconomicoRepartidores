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

    void verificarRolRepartidor(String usuarioId, Callback<Boolean> callback);
}
