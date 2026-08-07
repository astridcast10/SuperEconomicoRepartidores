package com.uth.supereconomicorepartidor.domain.entities;

public class ItemPedido {
    private final Long id;
    private final Long pedidoId;
    private final String productoNombre;
    private final int cantidad;
    private final double precioUnitario;

    public ItemPedido(Long id, Long pedidoId, String productoNombre, int cantidad, double precioUnitario) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public String getProductoNombre() { return productoNombre; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public double getSubtotal() { return cantidad * precioUnitario; }
}
