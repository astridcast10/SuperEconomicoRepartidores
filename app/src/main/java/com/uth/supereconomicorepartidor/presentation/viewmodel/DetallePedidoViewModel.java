package com.uth.supereconomicorepartidor.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.uth.supereconomicorepartidor.data.remote.models.DireccionDTO;
import com.uth.supereconomicorepartidor.domain.entities.ItemPedido;
import com.uth.supereconomicorepartidor.domain.entities.Usuario;
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

    private final MutableLiveData<Usuario> _cliente = new MutableLiveData<>();
    public LiveData<Usuario> cliente = _cliente;

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

    public void cargarDetalles(Long pedidoId, Long direccionId, String clienteId) {
        _isLoading.setValue(true);
        
        // Cargar items
        getItemsPedidoUseCase.execute(pedidoId, new RepartidorRepository.Callback<List<ItemPedido>>() {
            @Override
            public void onSuccess(List<ItemPedido> result) {
                _items.postValue(result);
                cargarDireccion(direccionId);
                cargarPerfilCliente(clienteId);
            }

            @Override
            public void onError(String message) {
                _isLoading.postValue(false);
                _error.postValue(message);
            }
        });
    }

    private void cargarDireccion(Long direccionId) {
        if (direccionId == null) {
            DireccionDTO errorDir = new DireccionDTO();
            errorDir.direccionTexto = "Dirección no disponible";
            _direccion.postValue(errorDir);
            return;
        }
        repository.getDireccion(direccionId, new RepartidorRepository.Callback<DireccionDTO>() {
            @Override
            public void onSuccess(DireccionDTO result) {
                _direccion.postValue(result);
            }
            @Override public void onError(String message) {
                _error.postValue("Error al cargar dirección: " + message);
                DireccionDTO errorDir = new DireccionDTO();
                errorDir.direccionTexto = "No se pudo cargar la dirección";
                _direccion.postValue(errorDir);
                _isLoading.postValue(false);
            }
        });
    }

    private void cargarPerfilCliente(String clienteId) {
        if (clienteId == null || clienteId.isEmpty()) {
            _isLoading.postValue(false);
            return;
        }
        repository.getPerfil(clienteId, new RepartidorRepository.Callback<Usuario>() {
            @Override
            public void onSuccess(Usuario result) {
                _cliente.postValue(result);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(String message) {
                _error.postValue("Error al cargar cliente: " + message);
                _isLoading.postValue(false);
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
