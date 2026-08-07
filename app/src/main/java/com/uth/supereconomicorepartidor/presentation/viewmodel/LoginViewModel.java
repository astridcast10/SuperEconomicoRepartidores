package com.uth.supereconomicorepartidor.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.uth.supereconomicorepartidor.domain.entities.Usuario;
import com.uth.supereconomicorepartidor.domain.repositories.AuthRepository;
import com.uth.supereconomicorepartidor.domain.usecases.LoginUseCase;

public class LoginViewModel extends ViewModel {
    private final LoginUseCase loginUseCase;

    private final MutableLiveData<Usuario> _user = new MutableLiveData<>();
    public LiveData<Usuario> user = _user;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    public LoginViewModel(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    public void login(String email, String password) {
        _isLoading.setValue(true);
        loginUseCase.execute(email, password, new AuthRepository.Callback<Usuario>() {
            @Override
            public void onSuccess(Usuario result) {
                _isLoading.postValue(false);
                _user.postValue(result);
            }
            @Override
            public void onError(String message) {
                _isLoading.postValue(false);
                _error.postValue(message);
            }
        });
    }
}
