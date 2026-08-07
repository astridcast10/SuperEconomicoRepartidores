package com.uth.supereconomicorepartidor.domain.entities;

public class Usuario {
    public enum Rol {
        CLIENTE,
        REPARTIDOR,
        ENCARGADO
    }

    private final String id;
    private final String email;
    private final String nombreCompleto;
    private final Rol rol;
    private final String telefono;

    public Usuario(String id, String email, String nombreCompleto, Rol rol, String telefono) {
        this.id = id;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.telefono = telefono;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getNombreCompleto() { return nombreCompleto; }
    public Rol getRol() { return rol; }
    public String getTelefono() { return telefono; }
}
