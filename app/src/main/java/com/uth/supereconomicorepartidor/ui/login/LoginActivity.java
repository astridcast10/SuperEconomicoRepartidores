package com.uth.supereconomicorepartidor.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.uth.supereconomicorepartidor.MainActivity;
import com.uth.supereconomicorepartidor.data.remote.SesionSupabase;
import com.uth.supereconomicorepartidor.databinding.ActivityLoginBinding;
import com.uth.supereconomicorepartidor.presentation.viewmodel.LoginViewModel;
import com.uth.supereconomicorepartidor.presentation.viewmodel.ViewModelFactory;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar sesión
        SesionSupabase.inicializar(this);

        // Si ya hay sesión activa, ir directo a la pantalla principal
        if (SesionSupabase.haySesionActiva()) {
            navegarAPedidos();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this, new ViewModelFactory()).get(LoginViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.user.observe(this, usuario -> {
            if (usuario != null) {
                navegarAPedidos();
            }
        });

        viewModel.error.observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnIngresar.setEnabled(!isLoading);
            binding.tilEmail.setEnabled(!isLoading);
            binding.tilPassword.setEnabled(!isLoading);
        });
    }

    private void setupListeners() {
        binding.btnIngresar.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            viewModel.login(email, password);
        });
    }

    private void navegarAPedidos() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finishAffinity();
    }
}
