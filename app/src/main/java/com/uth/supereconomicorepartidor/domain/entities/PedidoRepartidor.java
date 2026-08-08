package com.uth.supereconomicorepartidor.domain.entities;

import java.io.Serializable;

public class PedidoRepartidor implements Serializable {
    private final Long id;
    private final String perfilId;
    private final Long direccionId;
    private final String repartidorId;
    private final String estado;
    private final Double total;
    private final String creadoAt;
    private final String nombreCliente;

    public PedidoRepartidor(Long id, String perfilId, Long direccionId, String repartidorId,
                             String estado, Double total, String creadoAt, String nombreCliente) {
        this.id = id;
        this.perfilId = perfilId;
        this.direccionId = direccionId;
        this.repartidorId = repartidorId;
        this.estado = estado;
        this.total = total;
        this.creadoAt = creadoAt;
        this.nombreCliente = nombreCliente;
    }

    public Long getId() { return id; }
    public String getPerfilId() { return perfilId; }
    public Long getDireccionId() { return direccionId; }
    public String getRepartidorId() { return repartidorId; }
    public String getEstado() { return estado; }
    public Double getTotal() { return total; }
    public String getCreadoAt() { return creadoAt; }
    public String getNombreCliente() { return nombreCliente; }
}
