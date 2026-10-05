package com.ecogestion.app.models;

public class Especie {

    private int id;
    private String nombre;
    private String nombreCientifico;
    private String ecorregion;
    private int cantidadDisponible;
    private String descripcion;
    private String fechaRegistro;

    public Especie() {}

    public Especie(int id, String nombre, String nombreCientifico, String ecorregion,
                   int cantidadDisponible, String descripcion, String fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.nombreCientifico = nombreCientifico;
        this.ecorregion = ecorregion;
        this.cantidadDisponible = cantidadDisponible;
        this.descripcion = descripcion;
        this.fechaRegistro = fechaRegistro;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNombreCientifico() { return nombreCientifico; }
    public void setNombreCientifico(String nombreCientifico) { this.nombreCientifico = nombreCientifico; }

    public String getEcorregion() { return ecorregion; }
    public void setEcorregion(String ecorregion) { this.ecorregion = ecorregion; }

    public int getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(int cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getStockTexto() {
        return cantidadDisponible + " plantines";
    }

    public String getStockEstado() {
        if (cantidadDisponible == 0) return "SIN STOCK";
        if (cantidadDisponible < 10) return "BAJO";
        return "DISPONIBLE";
    }
}
