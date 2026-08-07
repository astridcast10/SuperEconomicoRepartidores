package com.uth.supereconomicorepartidor.data.remote.models;

import com.google.gson.annotations.SerializedName;
import com.uth.supereconomicorepartidor.domain.entities.ItemPedido;

public class ItemPedidoDTO {
    public Long id;
    @SerializedName("pedido_id")
    public Long pedidoId;
    @SerializedName("producto_nombre")
    public String productoNombre;
    public int cantidad;
    @SerializedName("precio_unitario")
    public double precioUnitario;

    public ItemPedido toDomain() {
        return new ItemPedido(id, pedidoId, productoNombre, cantidad, precioUnitario);
    }
}
