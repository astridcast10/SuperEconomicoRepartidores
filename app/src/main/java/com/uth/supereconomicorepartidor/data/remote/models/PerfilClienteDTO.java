package com.uth.supereconomicorepartidor.data.remote.models;

import com.google.gson.annotations.SerializedName;

public class PerfilClienteDTO {
    public String id;
    public String email;

    @SerializedName("nombre_completo")
    public String nombreCompleto;

    public String telefono;

    public String rol; // 'cliente' | 'encargado' | 'repartidor'
}
