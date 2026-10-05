package com.ecogestion.app.models;

public class Tarea {

    private int    id;
    private String titulo;
    private String descripcion;
    private String tipo;          // PLANTACION | RIEGO | PODA | INSPECCION | MANTENIMIENTO | OTRO
    private String estado;        // PENDIENTE | EN_CURSO | COMPLETADA | CANCELADA
    private String prioridad;     // BAJA | MEDIA | ALTA
    private int    zonaId;
    private String zonaNombre;    // join
    private int    plantacionId;
    private String plantacionNombre; // join
    private int    asignadoId;
    private String asignadoNombre;   // join
    private String fechaInicio;
    private String fechaLimite;
    private String observaciones;
    private String fechaRegistro;

    public Tarea() {}

    public Tarea(int id, String titulo, String descripcion,
                 String tipo, String estado, String prioridad,
                 int zonaId, String zonaNombre,
                 int plantacionId, String plantacionNombre,
                 int asignadoId, String asignadoNombre,
                 String fechaInicio, String fechaLimite,
                 String observaciones, String fechaRegistro) {
        this.id                = id;
        this.titulo            = titulo;
        this.descripcion       = descripcion;
        this.tipo              = tipo;
        this.estado            = estado;
        this.prioridad         = prioridad;
        this.zonaId            = zonaId;
        this.zonaNombre        = zonaNombre;
        this.plantacionId      = plantacionId;
        this.plantacionNombre  = plantacionNombre;
        this.asignadoId        = asignadoId;
        this.asignadoNombre    = asignadoNombre;
        this.fechaInicio       = fechaInicio;
        this.fechaLimite       = fechaLimite;
        this.observaciones     = observaciones;
        this.fechaRegistro     = fechaRegistro;
    }

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id = id; }

    public String getTitulo()                    { return titulo; }
    public void   setTitulo(String v)            { this.titulo = v; }

    public String getDescripcion()               { return descripcion; }
    public void   setDescripcion(String v)       { this.descripcion = v; }

    public String getTipo()                      { return tipo; }
    public void   setTipo(String v)              { this.tipo = v; }

    public String getEstado()                    { return estado; }
    public void   setEstado(String v)            { this.estado = v; }

    public String getPrioridad()                 { return prioridad; }
    public void   setPrioridad(String v)         { this.prioridad = v; }

    public int    getZonaId()                    { return zonaId; }
    public void   setZonaId(int v)               { this.zonaId = v; }

    public String getZonaNombre()                { return zonaNombre; }
    public void   setZonaNombre(String v)        { this.zonaNombre = v; }

    public int    getPlantacionId()              { return plantacionId; }
    public void   setPlantacionId(int v)         { this.plantacionId = v; }

    public String getPlantacionNombre()          { return plantacionNombre; }
    public void   setPlantacionNombre(String v)  { this.plantacionNombre = v; }

    public int    getAsignadoId()                { return asignadoId; }
    public void   setAsignadoId(int v)           { this.asignadoId = v; }

    public String getAsignadoNombre()            { return asignadoNombre; }
    public void   setAsignadoNombre(String v)    { this.asignadoNombre = v; }

    public String getFechaInicio()               { return fechaInicio; }
    public void   setFechaInicio(String v)       { this.fechaInicio = v; }

    public String getFechaLimite()               { return fechaLimite; }
    public void   setFechaLimite(String v)       { this.fechaLimite = v; }

    public String getObservaciones()             { return observaciones; }
    public void   setObservaciones(String v)     { this.observaciones = v; }

    public String getFechaRegistro()             { return fechaRegistro; }
    public void   setFechaRegistro(String v)     { this.fechaRegistro = v; }
}
