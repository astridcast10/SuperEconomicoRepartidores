package com.uth.supereconomicorepartidor.ui.pedidos;

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

import com.uth.supereconomicorepartidor.data.remote.SesionSupabase;
import com.uth.supereconomicorepartidor.databinding.FragmentPedidosBinding;
import com.uth.supereconomicorepartidor.presentation.viewmodel.PedidosViewModel;
import com.uth.supereconomicorepartidor.presentation.viewmodel.ViewModelFactory;

public class PedidosFragment extends Fragment {

    private FragmentPedidosBinding binding;
    private PedidosViewModel viewModel;
    private PedidosAdapter adapter;

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
        
        setupRecyclerView();
        setupObservers();
        setupListeners();

        cargarDatos();
    }

    private void setupRecyclerView() {
        adapter = new PedidosAdapter(pedido -> {
            // Acción al tocar un pedido (Fase 5)
            Toast.makeText(getContext(), "Pedido #" + pedido.getId(), Toast.LENGTH_SHORT).show();
        });
        binding.rvPedidos.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPedidos.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.pedidos.observe(getViewLifecycleOwner(), pedidos -> {
            adapter.submitList(pedidos);
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
