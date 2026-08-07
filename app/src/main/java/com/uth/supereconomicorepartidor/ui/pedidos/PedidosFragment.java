package com.uth.supereconomicorepartidor.ui.pedidos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.uth.supereconomicorepartidor.R;
import com.uth.supereconomicorepartidor.data.remote.SesionSupabase;
import com.uth.supereconomicorepartidor.databinding.FragmentPedidosBinding;
import com.uth.supereconomicorepartidor.presentation.viewmodel.PedidosViewModel;
import com.uth.supereconomicorepartidor.presentation.viewmodel.ViewModelFactory;
import com.uth.supereconomicorepartidor.ui.detalle.DetallePedidoFragment;
import com.uth.supereconomicorepartidor.ui.login.LoginActivity;

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
        
        setupToolbar();
        setupRecyclerView();
        setupObservers();
        setupListeners();

        cargarDatos();
    }

    private void setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_pedidos);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                SesionSupabase.cerrarSesion();
                startActivity(new Intent(getContext(), LoginActivity.class));
                if (getActivity() != null) getActivity().finish();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new PedidosAdapter(pedido -> {
            DetallePedidoFragment fragment = DetallePedidoFragment.newInstance(pedido);
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
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
