package com.uth.supereconomicorepartidor.ui.detalle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.uth.supereconomicorepartidor.R;
import com.uth.supereconomicorepartidor.data.remote.OsrmApi;
import com.uth.supereconomicorepartidor.data.remote.SesionSupabase;
import com.uth.supereconomicorepartidor.databinding.FragmentDetallePedidoBinding;
import com.uth.supereconomicorepartidor.domain.entities.PedidoRepartidor;
import com.uth.supereconomicorepartidor.presentation.viewmodel.DetallePedidoViewModel;
import com.uth.supereconomicorepartidor.presentation.viewmodel.ViewModelFactory;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetallePedidoFragment extends Fragment {

    private static final String ARG_PEDIDO = "pedido";

    private FragmentDetallePedidoBinding binding;
    private DetallePedidoViewModel viewModel;
    private ItemPedidoAdapter adapter;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private PedidoRepartidor pedido;
    private GeoPoint destinoGeoPoint;
    private Marker markerRepartidor;
    private Marker markerDestino;
    private Polyline routePolyline;
    private boolean isEnCamino = false;

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
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        if (getArguments() != null) {
            pedido = (PedidoRepartidor) getArguments().getSerializable(ARG_PEDIDO);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    GeoPoint current = new GeoPoint(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                    animarMarcadorRepartidor(current);
                }
            }
        };
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
        setupMap();

        if (pedido != null) {
            viewModel.cargarDetalles(pedido.getId(), pedido.getDireccionId(), pedido.getPerfilId());
            isEnCamino = "en_camino".equalsIgnoreCase(pedido.getEstado());
            if (isEnCamino) startLocationUpdates();
        }
    }

    private void setupUI() {
        if (pedido == null) return;
        binding.tvIdPedido.setText(String.format(Locale.getDefault(), "#%d", pedido.getId()));
        binding.tvTotal.setText(String.format(Locale.getDefault(), "Total a Cobrar: $%.2f", pedido.getTotal()));

        String[] estados = {"pendiente", "preparando", "en_camino", "entregado"};
        ArrayAdapter<String> adapterEstados = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, estados);
        binding.autoCompleteEstado.setAdapter(adapterEstados);
        binding.autoCompleteEstado.setText(pedido.getEstado(), false);

        binding.autoCompleteEstado.setOnItemClickListener((parent, view, position, id) -> {
            String nuevoEstado = estados[position];
            viewModel.actualizarEstado(pedido.getId(), nuevoEstado, SesionSupabase.obtenerIdUsuario());
            
            if ("en_camino".equalsIgnoreCase(nuevoEstado)) {
                isEnCamino = true;
                startLocationUpdates();
            } else {
                isEnCamino = false;
                stopLocationUpdates();
            }
        });
        
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        
        binding.fabCentrar.setOnClickListener(v -> centrarMapa());
    }

    private void setupRecyclerView() {
        adapter = new ItemPedidoAdapter();
        binding.rvProductos.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProductos.setAdapter(adapter);
    }

    private void setupMap() {
        binding.map.setTileSource(TileSourceFactory.MAPNIK);
        binding.map.setMultiTouchControls(true);
        binding.map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        binding.map.getController().setZoom(17.0);

        // BUG 3 - Evitar que el scroll del padre interfiera con el mapa
        binding.map.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
    }

    private void setupObservers() {
        viewModel.items.observe(getViewLifecycleOwner(), items -> adapter.submitList(items));

        viewModel.direccion.observe(getViewLifecycleOwner(), dir -> {
            if (dir != null) {
                binding.tvDireccion.setText(dir.direccionTexto);
                if (dir.latitud != null && dir.longitud != null) {
                    destinoGeoPoint = new GeoPoint(dir.latitud, dir.longitud);
                    actualizarMapaConDestino();
                }
            }
        });

        viewModel.cliente.observe(getViewLifecycleOwner(), cliente -> {
            if (cliente != null) {
                binding.tvClienteNombre.setText(cliente.getNombreCompleto());
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

    private void actualizarMapaConDestino() {
        if (destinoGeoPoint == null) return;

        if (markerDestino == null) {
            markerDestino = new Marker(binding.map);
            markerDestino.setTitle("Destino Entrega");
            markerDestino.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_location));
            markerDestino.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            binding.map.getOverlays().add(markerDestino);
        }
        markerDestino.setPosition(destinoGeoPoint);

        centrarMapa();
    }

    private void centrarMapa() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    GeoPoint current = new GeoPoint(location.getLatitude(), location.getLongitude());
                    if (markerRepartidor == null) {
                        markerRepartidor = new Marker(binding.map);
                        markerRepartidor.setTitle("Mi Ubicación (Moto)");
                        markerRepartidor.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_moto));
                        markerRepartidor.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                        binding.map.getOverlays().add(markerRepartidor);
                    }
                    markerRepartidor.setPosition(current);
                    binding.map.getController().animateTo(current);
                    trazarRuta(current, destinoGeoPoint);
                } else if (destinoGeoPoint != null) {
                    binding.map.getController().animateTo(destinoGeoPoint);
                }
            });
        } else if (destinoGeoPoint != null) {
            binding.map.getController().animateTo(destinoGeoPoint);
        }
    }

    private void animarMarcadorRepartidor(GeoPoint target) {
        if (markerRepartidor == null) {
            markerRepartidor = new Marker(binding.map);
            markerRepartidor.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_moto));
            markerRepartidor.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            binding.map.getOverlays().add(markerRepartidor);
        }
        
        GeoPoint start = markerRepartidor.getPosition();
        final long duration = 1500;
        final long startMillis = System.currentTimeMillis();
        
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startMillis;
                float t = Math.min(1f, (float) elapsed / duration);
                
                double lat = start.getLatitude() + (target.getLatitude() - start.getLatitude()) * t;
                double lon = start.getLongitude() + (target.getLongitude() - start.getLongitude()) * t;
                
                markerRepartidor.setPosition(new GeoPoint(lat, lon));
                binding.map.invalidate();
                
                if (t < 1f) {
                    handler.postDelayed(this, 16);
                } else {
                    trazarRuta(target, destinoGeoPoint);
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void trazarRuta(GeoPoint origin, GeoPoint dest) {
        if (origin == null || dest == null) return;
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://router.project-osrm.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OsrmApi api = retrofit.create(OsrmApi.class);
        String coords = origin.getLongitude() + "," + origin.getLatitude() + ";" + dest.getLongitude() + "," + dest.getLatitude();

        api.getRoute(coords, "full", "geojson").enqueue(new Callback<OsrmApi.OsrmResponse>() {
            @Override
            public void onResponse(Call<OsrmApi.OsrmResponse> call, Response<OsrmApi.OsrmResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().routes != null && !response.body().routes.isEmpty()) {
                    List<List<Double>> points = response.body().routes.get(0).geometry.coordinates;
                    List<GeoPoint> geoPoints = new ArrayList<>();
                    for (List<Double> point : points) {
                        geoPoints.add(new GeoPoint(point.get(1), point.get(0)));
                    }

                    if (routePolyline != null) binding.map.getOverlays().remove(routePolyline);
                    routePolyline = new Polyline();
                    routePolyline.setPoints(geoPoints);
                    routePolyline.getOutlinePaint().setColor(Color.parseColor("#2E7D32"));
                    routePolyline.getOutlinePaint().setStrokeWidth(12);
                    binding.map.getOverlays().add(routePolyline);
                    binding.map.invalidate();
                }
            }
            @Override public void onFailure(Call<OsrmApi.OsrmResponse> call, Throwable t) { }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.map.onResume();
        if (isEnCamino) startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.map.onPause();
        stopLocationUpdates();
    }
}
