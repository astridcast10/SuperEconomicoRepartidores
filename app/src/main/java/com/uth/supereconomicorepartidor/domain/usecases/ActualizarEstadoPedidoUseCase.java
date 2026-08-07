package com.uth.supereconomicorepartidor.domain.usecases;

import com.uth.supereconomicorepartidor.domain.repositories.RepartidorRepository;

public class ActualizarEstadoPedidoUseCase {
    private final RepartidorRepository repository;

    public ActualizarEstadoPedidoUseCase(RepartidorRepository repository) {
        this.repository = repository;
    }

    public void execute(Long pedidoId, String nuevoEstado, String repartidorId, RepartidorRepository.Callback<Void> callback) {
        repository.actualizarEstadoConRepartidor(pedidoId, nuevoEstado, repartidorId, callback);
    }
}
