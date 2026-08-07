package com.uth.supereconomicorepartidor.data.remote.models;

import com.google.gson.annotations.SerializedName;

public class DireccionDTO {
    public Long id;
    public String etiqueta;

    @SerializedName("direccion_texto")
    public String direccionTexto;

    public Double latitud;
    public Double longitud;
}
