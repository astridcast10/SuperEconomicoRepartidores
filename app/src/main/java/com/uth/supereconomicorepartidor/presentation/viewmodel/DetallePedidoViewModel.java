package com.uth.supereconomicorepartidor.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.uth.supereconomicorepartidor.data.remote.models.DireccionDTO;
import com.uth.supereconomicorepartidor.domain.entities.ItemPedido;
import com.uth.supereconomicorepartidor.domain.repositories.RepartidorRepository;
import com.uth.supereconomicorepartidor.domain.usecases.ActualizarEstadoPedidoUseCase;
import com.uth.supereconomicorepartidor.domain.usecases.GetItemsPedidoUseCase;
import java.util.List;

public class DetallePedidoViewModel extends ViewModel {
    private final RepartidorRepository repository;
    private final GetItemsPedidoUseCase getItemsPedidoUseCase;
    private final ActualizarEstadoPedidoUseCase actualizarEstadoPedidoUseCase;

    private final MutableLiveData<List<ItemPedido>> _items = new MutableLiveData<>();
    public LiveData<List<ItemPedido>> items = _items;

    private final MutableLiveData<DireccionDTO> _direccion = new MutableLiveData<>();
    public LiveData<DireccionDTO> direccion = _direccion;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _updateSuccess = new MutableLiveData<>();
    public LiveData<Boolean> updateSuccess = _updateSuccess;

    public DetallePedidoViewModel(RepartidorRepository repository, GetItemsPedidoUseCase getItemsPedidoUseCase, ActualizarEstadoPedidoUseCase actualizarEstadoPedidoUseCase) {
        this.repository = repository;
        this.getItemsPedidoUseCase = getItemsPedidoUseCase;
        this.actualizarEstadoPedidoUseCase = actualizarEstadoPedidoUseCase;
    }

    public void cargarDetalles(Long pedidoId, Long direccionId) {
        _isLoading.setValue(true);
        
        // Cargar items
        getItemsPedidoUseCase.execute(pedidoId, new RepartidorRepository.Callback<List<ItemPedido>>() {
            @Override
            public void onSuccess(List<ItemPedido> result) {
                _items.postValue(result);
                // Si ya tenemos items, intentamos cargar dirección
                cargarDireccion(direccionId);
            }

            @Override
            public void onError(String message) {
                _isLoading.postValue(false);
                _error.postValue(message);
            }
        });
    }

    private void cargarDireccion(Long direccionId) {
        repository.getDireccion(direccionId, new RepartidorRepository.Callback<DireccionDTO>() {
            @Override
            public void onSuccess(DireccionDTO result) {
                _isLoading.postValue(false);
                _direccion.postValue(result);
            }

            @Override
            public void onError(String message) {
                _isLoading.postValue(false);
                _error.postValue(message);
            }
        });
    }

    public void actualizarEstado(Long pedidoId, String nuevoEstado, String repartidorId) {
        _isLoading.setValue(true);
        actualizarEstadoPedidoUseCase.execute(pedidoId, nuevoEstado, repartidorId, new RepartidorRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _isLoading.postValue(false);
                _updateSuccess.postValue(true);
            }

            @Override
            public void onError(String message) {
                _isLoading.postValue(false);
                _error.postValue(message);
            }
        });
    }
}
