package com.uth.supereconomicorepartidor.ui.pedidos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.uth.supereconomicorepartidor.R;
import com.uth.supereconomicorepartidor.data.remote.SesionSupabase;
import com.uth.supereconomicorepartidor.databinding.FragmentPedidosBinding;
import com.uth.supereconomicorepartidor.domain.entities.PedidoRepartidor;
import com.uth.supereconomicorepartidor.presentation.viewmodel.PedidosViewModel;
import com.uth.supereconomicorepartidor.presentation.viewmodel.ViewModelFactory;
import com.uth.supereconomicorepartidor.ui.detalle.DetallePedidoFragment;
import com.uth.supereconomicorepartidor.ui.login.LoginActivity;

import java.util.ArrayList;
import java.util.List;

public class PedidosFragment extends Fragment {

    private FragmentPedidosBinding binding;
    private PedidosViewModel viewModel;
    private PedidosAdapter adapter;
    private List<PedidoRepartidor> allPedidos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPedidosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, new ViewModelFactory()).get(PedidosViewModel.class);
        
        setupUI();
        setupRecyclerView();
        setupObservers();
        setupListeners();

        cargarDatos();
    }

    private void setupUI() {
        String nombre = SesionSupabase.obtenerNombre();
        if (nombre != null && !nombre.isEmpty()) {
            binding.tvTitle.setText("Hola, " + nombre.split(" ")[0]);
        } else {
            binding.tvTitle.setText("Mis Pedidos");
        }
    }

    private void setupRecyclerView() {
        adapter = new PedidosAdapter(pedido -> {
            DetallePedidoFragment fragment = DetallePedidoFragment.newInstance(pedido);
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        binding.rvPedidos.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPedidos.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.pedidos.observe(getViewLifecycleOwner(), pedidos -> {
            this.allPedidos = pedidos != null ? pedidos : new ArrayList<>();
            filtrarPedidos(binding.tabLayout.getSelectedTabPosition());
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        viewModel.error.observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null) {
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::cargarDatos);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { filtrarPedidos(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.cvProfile.setOnClickListener(v -> {
            android.util.Log.d("Logout", "click detectado en cvProfile (dentro de Toolbar)");
            android.widget.Toast.makeText(getContext(), "Abriendo cierre de sesión...", android.widget.Toast.LENGTH_SHORT).show();
            mostrarDialogoLogout();
        });
    }

    private void mostrarDialogoLogout() {
        android.util.Log.d("Logout", "mostrando dialogo de confirmacion");
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas salir?")
                .setPositiveButton("Sí, salir", (dialog, which) -> {
                    android.util.Log.d("Logout", "confirmacion aceptada, ejecutando cierre");
                    SesionSupabase.cerrarSesion();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) getActivity().finish();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> android.util.Log.d("Logout", "confirmacion cancelada"))
                .show();
    }

    private void filtrarPedidos(int tabPosition) {
        List<PedidoRepartidor> filtrados = new ArrayList<>();
        for (PedidoRepartidor p : allPedidos) {
            boolean enCurso = "pendiente".equalsIgnoreCase(p.getEstado()) || 
                             "preparando".equalsIgnoreCase(p.getEstado()) || 
                             "en_camino".equalsIgnoreCase(p.getEstado());
            
            if (tabPosition == 0 && enCurso) {
                filtrados.add(p);
            } else if (tabPosition == 1 && !enCurso) {
                filtrados.add(p);
            }
        }
        adapter.submitList(filtrados);
    }

    private void cargarDatos() {
        String uid = SesionSupabase.obtenerIdUsuario();
        viewModel.cargarPedidos(uid);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
