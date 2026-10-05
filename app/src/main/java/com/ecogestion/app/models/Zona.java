package com.ecogestion.app.models;

public class Zona {

    private int id;
    private String nombre;
    private String departamento;
    private String localidad;
    private String latitud;
    private String longitud;
    private String estado; // ACTIVA, PENDIENTE, FINALIZADA
    private int responsableId;
    private String responsableNombre;
    private String fechaRegistro;

    public Zona() {}

    public Zona(int id, String nombre, String departamento, String localidad,
                String latitud, String longitud, String estado,
                int responsableId, String fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.departamento = departamento;
        this.localidad = localidad;
        this.latitud = latitud;
        this.longitud = longitud;
        this.estado = estado;
        this.responsableId = responsableId;
        this.fechaRegistro = fechaRegistro;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public String getLatitud() { return latitud; }
    public void setLatitud(String latitud) { this.latitud = latitud; }

    public String getLongitud() { return longitud; }
    public void setLongitud(String longitud) { this.longitud = longitud; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getResponsableId() { return responsableId; }
    public void setResponsableId(int responsableId) { this.responsableId = responsableId; }

    public String getResponsableNombre() { return responsableNombre; }
    public void setResponsableNombre(String responsableNombre) { this.responsableNombre = responsableNombre; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getUbicacionCompleta() {
        return departamento + " — " + localidad;
    }
}
