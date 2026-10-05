package com.ecogestion.app.models;

public class Auditoria {

    private int    id;
    private int    usuarioId;
    private String nombreUsuario;   // JOIN con tabla usuarios
    private String accion;
    private String detalle;
    private String fecha;

    public Auditoria(int id, int usuarioId, String nombreUsuario,
                     String accion, String detalle, String fecha) {
        this.id            = id;
        this.usuarioId     = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.accion        = accion;
        this.detalle       = detalle;
        this.fecha         = fecha;
    }

    public int    getId()            { return id; }
    public int    getUsuarioId()     { return usuarioId; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getAccion()        { return accion; }
    public String getDetalle()       { return detalle; }
    public String getFecha()         { return fecha; }

    /** Etiqueta legible de la acción para mostrar en pantalla. */
    public String getAccionLabel() {
        if (accion == null) return "Acción";
        switch (accion) {
            case "LOGIN":               return "Inicio de sesión";
            case "CREAR_PLANTACION":    return "Nueva plantación";
            case "MODIFICAR_PLANTACION":return "Editó plantación";
            case "ELIMINAR_PLANTACION": return "Eliminó plantación";
            case "CREAR_ZONA":          return "Nueva zona";
            case "MODIFICAR_ZONA":      return "Editó zona";
            case "ELIMINAR_ZONA":       return "Eliminó zona";
            case "CREAR_ESPECIE":       return "Nueva especie";
            case "MODIFICAR_ESPECIE":   return "Editó especie";
            case "ELIMINAR_ESPECIE":    return "Eliminó especie";
            case "CREAR_TAREA":         return "Nueva tarea";
            case "MODIFICAR_TAREA":     return "Editó tarea";
            case "ELIMINAR_TAREA":      return "Eliminó tarea";
            case "CREAR_USUARIO":       return "Nuevo usuario";
            case "MODIFICAR_USUARIO":   return "Editó usuario";
            case "ELIMINAR_USUARIO":    return "Eliminó usuario";
            default:                    return accion.replace("_", " ");
        }
    }

    /** Categoría de la acción: CREAR / MODIFICAR / ELIMINAR / SISTEMA */
    public String getCategoria() {
        if (accion == null) return "SISTEMA";
        if (accion.startsWith("CREAR"))     return "CREAR";
        if (accion.startsWith("MODIFICAR")) return "MODIFICAR";
        if (accion.startsWith("ELIMINAR"))  return "ELIMINAR";
        return "SISTEMA";
    }
}
