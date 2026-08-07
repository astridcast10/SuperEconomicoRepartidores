package com.uth.supereconomicorepartidor.data.remote.models;

import com.google.gson.annotations.SerializedName;
import com.uth.supereconomicorepartidor.domain.entities.PedidoRepartidor;

public class PedidoRepartidorDTO {

    public Long id;

    @SerializedName("perfil_id")
    public String perfilId;

    @SerializedName("direccion_id")
    public Long direccionId;

    @SerializedName("repartidor_id")
    public String repartidorId;

    public String estado;

    public Double total;

    @SerializedName("creado_at")
    public String creadoAt;

    public PedidoRepartidor toDomain() {
        return new PedidoRepartidor(
                id,
                perfilId,
                direccionId,
                repartidorId,
                estado,
                total,
                creadoAt
        );
    }
}
