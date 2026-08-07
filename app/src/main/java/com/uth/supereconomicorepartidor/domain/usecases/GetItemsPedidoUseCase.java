package com.uth.supereconomicorepartidor.domain.usecases;

import com.uth.supereconomicorepartidor.domain.entities.ItemPedido;
import com.uth.supereconomicorepartidor.domain.repositories.RepartidorRepository;
import java.util.List;

public class GetItemsPedidoUseCase {
    private final RepartidorRepository repository;

    public GetItemsPedidoUseCase(RepartidorRepository repository) {
        this.repository = repository;
    }

    public void execute(Long pedidoId, RepartidorRepository.Callback<List<ItemPedido>> callback) {
        repository.getItemsPedido(pedidoId, callback);
    }
}
