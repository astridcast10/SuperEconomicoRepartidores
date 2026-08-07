package com.uth.supereconomicorepartidor.domain.usecases;

import com.uth.supereconomicorepartidor.domain.entities.PedidoRepartidor;
import com.uth.supereconomicorepartidor.domain.repositories.RepartidorRepository;

import java.util.List;

public class GetPedidosUseCase {
    private final RepartidorRepository repository;

    public GetPedidosUseCase(RepartidorRepository repository) {
        this.repository = repository;
    }

    public void execute(String repartidorId, RepartidorRepository.Callback<List<PedidoRepartidor>> callback) {
        repository.getPedidosAsignados(repartidorId, callback);
    }
}
