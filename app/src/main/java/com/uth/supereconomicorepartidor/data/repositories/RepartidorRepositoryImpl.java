package com.uth.supereconomicorepartidor.data.repositories;

import com.google.gson.JsonObject;
import com.uth.supereconomicorepartidor.data.remote.RepartidorApi;
import com.uth.supereconomicorepartidor.data.remote.models.PedidoRepartidorDTO;
import com.uth.supereconomicorepartidor.data.remote.models.PerfilClienteDTO;
import com.uth.supereconomicorepartidor.domain.entities.PedidoRepartidor;
import com.uth.supereconomicorepartidor.domain.repositories.RepartidorRepository;
import com.uth.supereconomicorepartidor.utils.UserFriendlyError;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

public class RepartidorRepositoryImpl implements RepartidorRepository {

    private final RepartidorApi repartidorApi;

    public RepartidorRepositoryImpl(RepartidorApi repartidorApi) {
        this.repartidorApi = repartidorApi;
    }

    @Override
    public void getPedidosAsignados(String repartidorId, Callback<List<PedidoRepartidor>> callback) {
        repartidorApi.getPedidosAsignados(
                "eq." + repartidorId,
                "*,cliente:perfiles!perfil_id(nombre_completo)",
                "creado_at.desc"
        ).enqueue(new retrofit2.Callback<List<PedidoRepartidorDTO>>() {
            @Override
            public void onResponse(Call<List<PedidoRepartidorDTO>> call, Response<List<PedidoRepartidorDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PedidoRepartidor> domainList = new ArrayList<>();
                    for (PedidoRepartidorDTO dto : response.body()) {
                        domainList.add(dto.toDomain());
                    }
                    callback.onSuccess(domainList);
                } else {
                    callback.onError(UserFriendlyError.fromResponse(response, "No se pudieron cargar tus pedidos asignados"));
                }
            }

            @Override
            public void onFailure(Call<List<PedidoRepartidorDTO>> call, Throwable t) {
                callback.onError(UserFriendlyError.fromThrowable(t));
            }
        });
    }

    @Override
    public void actualizarEstado(Long pedidoId, String nuevoEstado, Callback<Void> callback) {
        if (pedidoId == null) return;
        JsonObject cambios = new JsonObject();
        cambios.addProperty("estado", nuevoEstado);

        repartidorApi.actualizarEstadoPedido("eq." + pedidoId, cambios).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(UserFriendlyError.fromResponse(response, "No se pudo actualizar el estado del pedido"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(UserFriendlyError.fromThrowable(t));
            }
        });
    }

    @Override
    public void actualizarEstadoConRepartidor(Long pedidoId, String nuevoEstado, String repartidorId, Callback<Void> callback) {
        if (pedidoId == null) return;
        JsonObject cambios = new JsonObject();
        cambios.addProperty("estado", nuevoEstado);
        cambios.addProperty("repartidor_id", repartidorId);

        repartidorApi.actualizarEstadoRepartidor("eq." + pedidoId, cambios).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(UserFriendlyError.fromResponse(response, "No se pudo actualizar el estado"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(UserFriendlyError.fromThrowable(t));
            }
        });
    }

    @Override
    public void getItemsPedido(Long pedidoId, Callback<List<com.uth.supereconomicorepartidor.domain.entities.ItemPedido>> callback) {
        repartidorApi.getItemsPedido("eq." + pedidoId, "*,productos(nombre)").enqueue(new retrofit2.Callback<List<com.uth.supereconomicorepartidor.data.remote.models.ItemPedidoDTO>>() {
            @Override
            public void onResponse(Call<List<com.uth.supereconomicorepartidor.data.remote.models.ItemPedidoDTO>> call, Response<List<com.uth.supereconomicorepartidor.data.remote.models.ItemPedidoDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<com.uth.supereconomicorepartidor.domain.entities.ItemPedido> items = new ArrayList<>();
                    for (com.uth.supereconomicorepartidor.data.remote.models.ItemPedidoDTO dto : response.body()) {
                        items.add(dto.toDomain());
                    }
                    callback.onSuccess(items);
                } else {
                    callback.onError(UserFriendlyError.fromResponse(response, "No se pudieron cargar los productos"));
                }
            }

            @Override
            public void onFailure(Call<List<com.uth.supereconomicorepartidor.data.remote.models.ItemPedidoDTO>> call, Throwable t) {
                callback.onError(UserFriendlyError.fromThrowable(t));
            }
        });
    }

    @Override
    public void getDireccion(Long direccionId, Callback<com.uth.supereconomicorepartidor.data.remote.models.DireccionDTO> callback) {
        repartidorApi.getDireccionPorId("eq." + direccionId, "*").enqueue(new retrofit2.Callback<List<com.uth.supereconomicorepartidor.data.remote.models.DireccionDTO>>() {
            @Override
            public void onResponse(Call<List<com.uth.supereconomicorepartidor.data.remote.models.DireccionDTO>> call, Response<List<com.uth.supereconomicorepartidor.data.remote.models.DireccionDTO>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError(UserFriendlyError.fromResponse(response, "No se pudo cargar la dirección"));
                }
            }

            @Override
            public void onFailure(Call<List<com.uth.supereconomicorepartidor.data.remote.models.DireccionDTO>> call, Throwable t) {
                callback.onError(UserFriendlyError.fromThrowable(t));
            }
        });
    }

    @Override
    public void verificarRolRepartidor(String usuarioId, Callback<Boolean> callback) {
        repartidorApi.getRolUsuario("eq." + usuarioId, "rol,nombre_completo").enqueue(new retrofit2.Callback<List<PerfilClienteDTO>>() {
            @Override
            public void onResponse(Call<List<PerfilClienteDTO>> call, Response<List<PerfilClienteDTO>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    boolean esRepartidor = "repartidor".equals(response.body().get(0).rol);
                    callback.onSuccess(esRepartidor);
                } else {
                    callback.onError(UserFriendlyError.fromResponse(response, "No se pudo verificar el perfil del usuario"));
                }
            }

            @Override
            public void onFailure(Call<List<PerfilClienteDTO>> call, Throwable t) {
                callback.onError(UserFriendlyError.fromThrowable(t));
            }
        });
    }

    @Override
    public void getPerfil(String usuarioId, Callback<com.uth.supereconomicorepartidor.domain.entities.Usuario> callback) {
        repartidorApi.getRolUsuario("eq." + usuarioId, "*").enqueue(new retrofit2.Callback<List<PerfilClienteDTO>>() {
            @Override
            public void onResponse(Call<List<PerfilClienteDTO>> call, Response<List<PerfilClienteDTO>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0).toDomain());
                } else {
                    callback.onError(UserFriendlyError.fromResponse(response, "No se pudo cargar el perfil"));
                }
            }

            @Override
            public void onFailure(Call<List<PerfilClienteDTO>> call, Throwable t) {
                callback.onError(UserFriendlyError.fromThrowable(t));
            }
        });
    }
}
