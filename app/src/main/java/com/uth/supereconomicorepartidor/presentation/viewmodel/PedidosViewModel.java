package com.uth.supereconomicorepartidor.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.uth.supereconomicorepartidor.domain.entities.PedidoRepartidor;
import com.uth.supereconomicorepartidor.domain.repositories.RepartidorRepository;
import com.uth.supereconomicorepartidor.domain.usecases.GetPedidosUseCase;

import java.util.List;

public class PedidosViewModel extends ViewModel {
    private final GetPedidosUseCase getPedidosUseCase;

    private final MutableLiveData<List<PedidoRepartidor>> _pedidos = new MutableLiveData<>();
    public LiveData<List<PedidoRepartidor>> pedidos = _pedidos;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public PedidosViewModel(GetPedidosUseCase getPedidosUseCase) {
        this.getPedidosUseCase = getPedidosUseCase;
    }

    public void cargarPedidos(String repartidorId) {
        if (repartidorId == null || repartidorId.isEmpty()) {
            _error.setValue("No se encontró el ID del repartidor");
            return;
        }
        
        _isLoading.setValue(true);
        getPedidosUseCase.execute(repartidorId, new RepartidorRepository.Callback<List<PedidoRepartidor>>() {
            @Override
            public void onSuccess(List<PedidoRepartidor> result) {
                _isLoading.postValue(false);
                _pedidos.postValue(result);
            }

            @Override
            public void onError(String message) {
                _isLoading.postValue(false);
                _error.postValue(message);
            }
        });
    }
}
