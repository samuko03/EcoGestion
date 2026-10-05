package com.ecogestion.app.utils;

/**
 * Centraliza todas las reglas de permisos por rol.
 *
 * Jerarquía:
 *   ADMIN        → acceso total
 *   COORDINADOR  → todo excepto gestionar usuarios
 *   SUPERVISOR   → crear/editar plantaciones y tareas; solo lectura en zonas/especies
 *   INSPECTOR    → solo lectura en todo; puede exportar reportes
 *   OPERADOR     → solo lectura en todo; sin acceso a reportes
 */
public class RolHelper {

    // ── Usuarios ───────────────────────────────────────────────
    public static boolean puedeVerMenuUsuarios(String rol) {
        return es(rol, "ADMIN");
    }

    // ── Zonas ──────────────────────────────────────────────────
    public static boolean puedeEditarZonas(String rol) {
        return esAdminO(rol, "COORDINADOR");
    }

    // ── Especies ───────────────────────────────────────────────
    public static boolean puedeEditarEspecies(String rol) {
        return esAdminO(rol, "COORDINADOR");
    }

    // ── Plantaciones ───────────────────────────────────────────
    public static boolean puedeCrearPlantaciones(String rol) {
        return esAdminO(rol, "COORDINADOR", "SUPERVISOR");
    }

    public static boolean puedeEliminarPlantaciones(String rol) {
        return esAdminO(rol, "COORDINADOR");
    }

    // ── Tareas ─────────────────────────────────────────────────
    public static boolean puedeCrearTareas(String rol) {
        return esAdminO(rol, "COORDINADOR", "SUPERVISOR");
    }

    public static boolean puedeEliminarTareas(String rol) {
        return esAdminO(rol, "COORDINADOR");
    }

    // ── Reportes ───────────────────────────────────────────────
    public static boolean puedeVerReportes(String rol) {
        return !es(rol, "OPERADOR");
    }

    public static boolean puedeExportarReportes(String rol) {
        return esAdminO(rol, "COORDINADOR", "SUPERVISOR", "INSPECTOR");
    }

    // ── Etiqueta legible del rol ───────────────────────────────
    public static String etiqueta(String rol) {
        if (rol == null) return "Sin rol";
        switch (rol.toUpperCase()) {
            case "ADMIN":       return "Administrador";
            case "COORDINADOR": return "Coordinador/a";
            case "SUPERVISOR":
            case "SUPERVISORA": return "Supervisor/a";
            case "INSPECTOR":   return "Inspector/a";
            case "OPERADOR":    return "Operador/a";
            default:            return rol;
        }
    }

    // ── Helpers privados ───────────────────────────────────────
    private static boolean es(String rol, String... roles) {
        if (rol == null) return false;
        for (String r : roles) {
            if (rol.equalsIgnoreCase(r)) return true;
        }
        return false;
    }

    private static boolean esAdminO(String rol, String... otros) {
        if (es(rol, "ADMIN")) return true;
        return es(rol, otros);
    }
}
