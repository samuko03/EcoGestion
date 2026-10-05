package com.ecogestion.app.models;

public class Plantacion {

    private int    id;
    private String nombre;
    private int    zonaId;
    private String zonaNombre;    // join
    private int    especieId;
    private String especieNombre; // join
    private int    cantidadArboles;
    private String fechaPlantacion;
    private String estado;        // PLANIFICADA | EN_PROCESO | COMPLETADA | CANCELADA
    private int    responsableId;
    private String responsableNombre; // join
    private String observaciones;
    private double latitud;
    private double longitud;
    private String fechaRegistro;

    public Plantacion() {}

    public Plantacion(int id, String nombre,
                      int zonaId, String zonaNombre,
                      int especieId, String especieNombre,
                      int cantidadArboles, String fechaPlantacion,
                      String estado, int responsableId, String responsableNombre,
                      String observaciones, String fechaRegistro) {
        this.id               = id;
        this.nombre           = nombre;
        this.zonaId           = zonaId;
        this.zonaNombre       = zonaNombre;
        this.especieId        = especieId;
        this.especieNombre    = especieNombre;
        this.cantidadArboles  = cantidadArboles;
        this.fechaPlantacion  = fechaPlantacion;
        this.estado           = estado;
        this.responsableId    = responsableId;
        this.responsableNombre= responsableNombre;
        this.observaciones    = observaciones;
        this.fechaRegistro    = fechaRegistro;
    }

    // Getters y setters
    public int    getId()                    { return id; }
    public void   setId(int id)              { this.id = id; }

    public String getNombre()                { return nombre; }
    public void   setNombre(String v)        { this.nombre = v; }

    public int    getZonaId()               { return zonaId; }
    public void   setZonaId(int v)          { this.zonaId = v; }

    public String getZonaNombre()           { return zonaNombre; }
    public void   setZonaNombre(String v)   { this.zonaNombre = v; }

    public int    getEspecieId()            { return especieId; }
    public void   setEspecieId(int v)       { this.especieId = v; }

    public String getEspecieNombre()        { return especieNombre; }
    public void   setEspecieNombre(String v){ this.especieNombre = v; }

    public int    getCantidadArboles()      { return cantidadArboles; }
    public void   setCantidadArboles(int v) { this.cantidadArboles = v; }

    public String getFechaPlantacion()      { return fechaPlantacion; }
    public void   setFechaPlantacion(String v){ this.fechaPlantacion = v; }

    public String getEstado()               { return estado; }
    public void   setEstado(String v)       { this.estado = v; }

    public int    getResponsableId()        { return responsableId; }
    public void   setResponsableId(int v)   { this.responsableId = v; }

    public String getResponsableNombre()    { return responsableNombre; }
    public void   setResponsableNombre(String v){ this.responsableNombre = v; }

    public String getObservaciones()        { return observaciones; }
    public void   setObservaciones(String v){ this.observaciones = v; }

    public double getLatitud()              { return latitud; }
    public void   setLatitud(double v)      { this.latitud = v; }

    public double getLongitud()             { return longitud; }
    public void   setLongitud(double v)     { this.longitud = v; }

    public boolean tieneUbicacion()         { return latitud != 0.0 || longitud != 0.0; }

    public String getFechaRegistro()        { return fechaRegistro; }
    public void   setFechaRegistro(String v){ this.fechaRegistro = v; }
}
