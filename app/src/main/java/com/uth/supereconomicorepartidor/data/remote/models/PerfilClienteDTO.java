package com.uth.supereconomicorepartidor.data.remote.models;

import com.google.gson.annotations.SerializedName;
import com.uth.supereconomicorepartidor.domain.entities.Usuario;

public class PerfilClienteDTO {
    public String id;
    public String email;

    @SerializedName("nombre_completo")
    public String nombreCompleto;

    public String telefono;

    public String rol; // 'cliente' | 'encargado' | 'repartidor'

    public Usuario toDomain() {
        Usuario.Rol rolEnum;
        try {
            rolEnum = Usuario.Rol.valueOf(rol == null ? "" : rol.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            rolEnum = null;
        }
        return new Usuario(id, email, nombreCompleto, rolEnum, telefono);
    }
}
