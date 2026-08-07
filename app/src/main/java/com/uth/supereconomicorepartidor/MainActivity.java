package com.uth.supereconomicorepartidor;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.uth.supereconomicorepartidor.data.remote.SesionSupabase;
import com.uth.supereconomicorepartidor.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializar la sesión de Supabase
        SesionSupabase.inicializar(this);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // El FragmentContainerView en el XML cargará PedidosFragment automáticamente
    }
}
