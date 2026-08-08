package com.uth.supereconomicorepartidor.data.remote.models;

import com.google.gson.annotations.SerializedName;
import com.uth.supereconomicorepartidor.domain.entities.ItemPedido;

public class ItemPedidoDTO {
    public Long id;
    @SerializedName("pedido_id")
    public Long pedidoId;
    
    // Cambiado para soportar el join: productos(nombre)
    public ProductoInnerDTO productos;
    
    public int cantidad;
    @SerializedName("precio_unitario")
    public double precioUnitario;

    public static class ProductoInnerDTO {
        public String nombre;
    }

    public ItemPedido toDomain() {
        String nombre = (productos != null) ? productos.nombre : "Producto Desconocido";
        return new ItemPedido(id, pedidoId, nombre, cantidad, precioUnitario);
    }
}
