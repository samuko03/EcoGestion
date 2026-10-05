package com.ecogestion.app.database.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.models.Auditoria;

import java.util.ArrayList;
import java.util.List;

public class AuditoriaDAO {

    private final DatabaseHelper dbHelper;

    public AuditoriaDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /** Todos los registros, del más reciente al más antiguo. */
    public List<Auditoria> obtenerTodos() {
        return consultar(null, null);
    }

    /** Filtrado por categoría (CREAR / MODIFICAR / ELIMINAR / SISTEMA / null = todos). */
    public List<Auditoria> obtenerPorCategoria(String categoria) {
        if (categoria == null || categoria.equals("TODOS")) {
            return obtenerTodos();
        }
        String where = "a." + DatabaseHelper.COL_AUD_ACCION + " LIKE ?";
        return consultar(where, new String[]{ categoria + "%" });
    }

    /**
     * Filtrado combinado: categoría + búsqueda libre.
     * La búsqueda aplica sobre nombre de usuario, detalle y acción.
     */
    public List<Auditoria> obtenerFiltrados(String categoria, String busqueda) {
        boolean tieneCategoria = categoria != null && !categoria.equals("TODOS");
        boolean tieneBusqueda  = busqueda  != null && !busqueda.trim().isEmpty();

        StringBuilder where = new StringBuilder();
        List<String> args   = new ArrayList<>();

        if (tieneCategoria) {
            where.append("a.").append(DatabaseHelper.COL_AUD_ACCION).append(" LIKE ?");
            args.add(categoria + "%");
        }

        if (tieneBusqueda) {
            String like = "%" + busqueda.trim() + "%";
            if (where.length() > 0) where.append(" AND ");
            where.append("(")
                 .append("(u.").append(DatabaseHelper.COL_NOMBRE)
                 .append(" || ' ' || u.").append(DatabaseHelper.COL_APELLIDO).append(") LIKE ?")
                 .append(" OR a.").append(DatabaseHelper.COL_AUD_DETALLE).append(" LIKE ?")
                 .append(" OR a.").append(DatabaseHelper.COL_AUD_ACCION).append(" LIKE ?")
                 .append(")");
            args.add(like);
            args.add(like);
            args.add(like);
        }

        return consultar(
                where.length() > 0 ? where.toString() : null,
                args.isEmpty() ? null : args.toArray(new String[0])
        );
    }

    /** Total de registros. */
    public int contarTodos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_AUDITORIA, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    // ── Helpers ────────────────────────────────────────────────

    private List<Auditoria> consultar(String where, String[] args) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT a." + DatabaseHelper.COL_AUD_ID + ", "
                + "a." + DatabaseHelper.COL_AUD_USUARIO_ID + ", "
                + "(u." + DatabaseHelper.COL_NOMBRE + " || ' ' || u." + DatabaseHelper.COL_APELLIDO + ") AS usuario_nombre, "
                + "a." + DatabaseHelper.COL_AUD_ACCION + ", "
                + "a." + DatabaseHelper.COL_AUD_DETALLE + ", "
                + "a." + DatabaseHelper.COL_AUD_FECHA
                + " FROM " + DatabaseHelper.TABLE_AUDITORIA + " a"
                + " LEFT JOIN " + DatabaseHelper.TABLE_USUARIOS + " u"
                + " ON a." + DatabaseHelper.COL_AUD_USUARIO_ID + " = u." + DatabaseHelper.COL_ID;

        if (where != null) sql += " WHERE " + where;
        sql += " ORDER BY a." + DatabaseHelper.COL_AUD_FECHA + " DESC";

        Cursor c = db.rawQuery(sql, args);
        List<Auditoria> lista = new ArrayList<>();
        while (c.moveToNext()) {
            lista.add(new Auditoria(
                    c.getInt(0),
                    c.getInt(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5)
            ));
        }
        c.close();
        return lista;
    }
}
