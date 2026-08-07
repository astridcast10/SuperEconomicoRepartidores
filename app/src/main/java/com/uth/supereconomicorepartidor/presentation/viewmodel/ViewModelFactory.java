package com.uth.supereconomicorepartidor.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.uth.supereconomicorepartidor.data.remote.AuthApi;
import com.uth.supereconomicorepartidor.data.remote.RepartidorApi;
import com.uth.supereconomicorepartidor.data.remote.RetrofitClient;
import com.uth.supereconomicorepartidor.data.repositories.AuthRepositoryImpl;
import com.uth.supereconomicorepartidor.data.repositories.RepartidorRepositoryImpl;
import com.uth.supereconomicorepartidor.domain.repositories.AuthRepository;
import com.uth.supereconomicorepartidor.domain.repositories.RepartidorRepository;
import com.uth.supereconomicorepartidor.domain.usecases.GetPedidosUseCase;
import com.uth.supereconomicorepartidor.domain.usecases.LoginUseCase;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final AuthRepository authRepository;
    private final RepartidorRepository repartidorRepository;

    public ViewModelFactory() {
        AuthApi authApi = RetrofitClient.getClient().create(AuthApi.class);
        RepartidorApi repartidorApi = RetrofitClient.getClient().create(RepartidorApi.class);
        
        this.authRepository = new AuthRepositoryImpl(authApi);
        this.repartidorRepository = new RepartidorRepositoryImpl(repartidorApi);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(new LoginUseCase(authRepository));
        } else if (modelClass.isAssignableFrom(PedidosViewModel.class)) {
            return (T) new PedidosViewModel(new GetPedidosUseCase(repartidorRepository));
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
