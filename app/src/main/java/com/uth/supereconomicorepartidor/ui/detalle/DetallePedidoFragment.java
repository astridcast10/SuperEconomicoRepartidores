package com.uth.supereconomicorepartidor.ui.detalle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.uth.supereconomicorepartidor.R;
import com.uth.supereconomicorepartidor.data.remote.GoogleMapsApi;
import com.uth.supereconomicorepartidor.data.remote.MapsConfig;
import com.uth.supereconomicorepartidor.data.remote.SesionSupabase;
import com.uth.supereconomicorepartidor.data.remote.models.DireccionDTO;
import com.uth.supereconomicorepartidor.databinding.FragmentDetallePedidoBinding;
import com.uth.supereconomicorepartidor.domain.entities.PedidoRepartidor;
import com.uth.supereconomicorepartidor.presentation.viewmodel.DetallePedidoViewModel;
import com.uth.supereconomicorepartidor.presentation.viewmodel.ViewModelFactory;
import com.uth.supereconomicorepartidor.utils.PolylineDecoder;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetallePedidoFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_PEDIDO = "pedido";

    private FragmentDetallePedidoBinding binding;
    private DetallePedidoViewModel viewModel;
    private ItemPedidoAdapter adapter;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private PedidoRepartidor pedido;
    private LatLng destinoLatLng;

    public static DetallePedidoFragment newInstance(PedidoRepartidor pedido) {
        DetallePedidoFragment fragment = new DetallePedidoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PEDIDO, pedido);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pedido = (PedidoRepartidor) getArguments().getSerializable(ARG_PEDIDO);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetallePedidoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, new ViewModelFactory()).get(DetallePedidoViewModel.class);

        setupUI();
        setupRecyclerView();
        setupObservers();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        if (pedido != null) {
            viewModel.cargarDetalles(pedido.getId(), pedido.getDireccionId());
        }
    }

    private void setupUI() {
        if (pedido == null) return;
        binding.tvIdPedido.setText(String.format(Locale.getDefault(), "#%d", pedido.getId()));
        binding.tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", pedido.getTotal()));

        String[] estados = {"pendiente", "en_camino", "entregado"};
        ArrayAdapter<String> adapterEstados = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, estados);
        binding.autoCompleteEstado.setAdapter(adapterEstados);
        binding.autoCompleteEstado.setText(pedido.getEstado(), false);

        binding.autoCompleteEstado.setOnItemClickListener((parent, view, position, id) -> {
            String nuevoEstado = estados[position];
            viewModel.actualizarEstado(pedido.getId(), nuevoEstado, SesionSupabase.obtenerIdUsuario());
        });
        
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new ItemPedidoAdapter();
        binding.rvProductos.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProductos.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.items.observe(getViewLifecycleOwner(), items -> adapter.submitList(items));

        viewModel.direccion.observe(getViewLifecycleOwner(), dir -> {
            if (dir != null) {
                binding.tvDireccion.setText(dir.direccionTexto);
                if (dir.latitud != null && dir.longitud != null) {
                    destinoLatLng = new LatLng(dir.latitud, dir.longitud);
                    actualizarMapaConDestino();
                }
            }
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.error.observe(getViewLifecycleOwner(), message -> {
            if (message != null) Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        });

        viewModel.updateSuccess.observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Snackbar.make(binding.getRoot(), "Estado actualizado correctamente", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        googleMap.setMyLocationEnabled(true);
        actualizarMapaConDestino();
    }

    private void actualizarMapaConDestino() {
        if (googleMap == null || destinoLatLng == null) return;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(current).title("Mi Ubicación"));
                    googleMap.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 14f));
                    trazarRuta(current, destinoLatLng);
                } else {
                    googleMap.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinoLatLng, 15f));
                }
            });
        } else {
            googleMap.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinoLatLng, 15f));
        }
    }

    private void trazarRuta(LatLng origin, LatLng dest) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GoogleMapsApi api = retrofit.create(GoogleMapsApi.class);
        String originStr = origin.latitude + "," + origin.longitude;
        String destStr = dest.latitude + "," + dest.longitude;

        api.getDirections(originStr, destStr, MapsConfig.GOOGLE_MAPS_KEY).enqueue(new Callback<GoogleMapsApi.DirectionsResponse>() {
            @Override
            public void onResponse(Call<GoogleMapsApi.DirectionsResponse> call, Response<GoogleMapsApi.DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().routes != null && !response.body().routes.isEmpty()) {
                    String points = response.body().routes.get(0).overviewPolyline.points;
                    List<LatLng> decodedPath = PolylineDecoder.decode(points);
                    googleMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(Color.GREEN).width(12));
                }
            }
            @Override
            public void onFailure(Call<GoogleMapsApi.DirectionsResponse> call, Throwable t) { }
        });
    }
}
