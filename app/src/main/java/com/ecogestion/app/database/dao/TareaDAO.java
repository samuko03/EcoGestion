package com.ecogestion.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.models.Tarea;

import java.util.ArrayList;
import java.util.List;

public class TareaDAO {

    private final DatabaseHelper dbHelper;

    public TareaDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ── CRUD ───────────────────────────────────────────────────
    public long insertar(Tarea t) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(DatabaseHelper.TABLE_TAREAS, null, toContentValues(t));
    }

    public int actualizar(Tarea t) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(DatabaseHelper.TABLE_TAREAS, toContentValues(t),
                DatabaseHelper.COL_TAR_ID + " = ?",
                new String[]{String.valueOf(t.getId())});
    }

    public int eliminar(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_TAREAS,
                DatabaseHelper.COL_TAR_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // ── Consultas ──────────────────────────────────────────────
    public List<Tarea> obtenerTodas() {
        List<Tarea> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryConJoins(null), null);
        while (cursor.moveToNext()) lista.add(cursorToTarea(cursor));
        cursor.close();
        return lista;
    }

    public List<Tarea> obtenerPorEstado(String estado) {
        List<Tarea> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryConJoins("t." + DatabaseHelper.COL_TAR_ESTADO + " = ?"),
                new String[]{estado});
        while (cursor.moveToNext()) lista.add(cursorToTarea(cursor));
        cursor.close();
        return lista;
    }

    public List<Tarea> obtenerPorAsignado(int usuarioId) {
        List<Tarea> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryConJoins("t." + DatabaseHelper.COL_TAR_ASIGNADO_ID + " = ?"),
                new String[]{String.valueOf(usuarioId)});
        while (cursor.moveToNext()) lista.add(cursorToTarea(cursor));
        cursor.close();
        return lista;
    }

    public Tarea obtenerPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryConJoins("t." + DatabaseHelper.COL_TAR_ID + " = ?"),
                new String[]{String.valueOf(id)});
        Tarea t = null;
        if (cursor.moveToFirst()) t = cursorToTarea(cursor);
        cursor.close();
        return t;
    }

    public int contarPorEstado(String estado) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TAREAS
                        + " WHERE " + DatabaseHelper.COL_TAR_ESTADO + " = ?",
                new String[]{estado});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // ── Helpers ────────────────────────────────────────────────
    private String queryConJoins(String whereClause) {
        String sql = "SELECT t.*, "
                + "z."  + DatabaseHelper.COL_ZONA_NOMBRE  + " AS zona_nombre, "
                + "pl." + DatabaseHelper.COL_PL_NOMBRE    + " AS plantacion_nombre, "
                + "(u." + DatabaseHelper.COL_NOMBRE + " || ' ' || u." + DatabaseHelper.COL_APELLIDO + ") AS asignado_nombre "
                + "FROM " + DatabaseHelper.TABLE_TAREAS + " t "
                + "LEFT JOIN " + DatabaseHelper.TABLE_ZONAS        + " z  ON t." + DatabaseHelper.COL_TAR_ZONA_ID       + " = z."  + DatabaseHelper.COL_ZONA_ID + " "
                + "LEFT JOIN " + DatabaseHelper.TABLE_PLANTACIONES + " pl ON t." + DatabaseHelper.COL_TAR_PLANTACION_ID + " = pl." + DatabaseHelper.COL_PL_ID   + " "
                + "LEFT JOIN " + DatabaseHelper.TABLE_USUARIOS     + " u  ON t." + DatabaseHelper.COL_TAR_ASIGNADO_ID   + " = u."  + DatabaseHelper.COL_ID;
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        // Prioridad: ALTA primero, luego fecha límite
        sql += " ORDER BY CASE t." + DatabaseHelper.COL_TAR_PRIORIDAD
                + " WHEN 'ALTA' THEN 1 WHEN 'MEDIA' THEN 2 ELSE 3 END, "
                + "t." + DatabaseHelper.COL_TAR_FECHA_LIMITE + " ASC";
        return sql;
    }

    private ContentValues toContentValues(Tarea t) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_TAR_TITULO,        t.getTitulo());
        cv.put(DatabaseHelper.COL_TAR_DESCRIPCION,   t.getDescripcion());
        cv.put(DatabaseHelper.COL_TAR_TIPO,          t.getTipo());
        cv.put(DatabaseHelper.COL_TAR_ESTADO,        t.getEstado());
        cv.put(DatabaseHelper.COL_TAR_PRIORIDAD,     t.getPrioridad());
        cv.put(DatabaseHelper.COL_TAR_ZONA_ID,       t.getZonaId() > 0 ? t.getZonaId() : null);
        cv.put(DatabaseHelper.COL_TAR_PLANTACION_ID, t.getPlantacionId() > 0 ? t.getPlantacionId() : null);
        cv.put(DatabaseHelper.COL_TAR_ASIGNADO_ID,   t.getAsignadoId() > 0 ? t.getAsignadoId() : null);
        cv.put(DatabaseHelper.COL_TAR_FECHA_INICIO,  t.getFechaInicio());
        cv.put(DatabaseHelper.COL_TAR_FECHA_LIMITE,  t.getFechaLimite());
        cv.put(DatabaseHelper.COL_TAR_OBSERVACIONES, t.getObservaciones());
        return cv;
    }

    private Tarea cursorToTarea(Cursor c) {
        return new Tarea(
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAR_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAR_TITULO)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAR_DESCRIPCION)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAR_TIPO)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAR_ESTADO)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAR_PRIORIDAD)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAR_ZONA_ID)),
                c.getString(c.getColumnIndexOrThrow("zona_nombre")),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAR_PLANTACION_ID)),
                c.getString(c.getColumnIndexOrThrow("plantacion_nombre")),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAR_ASIGNADO_ID)),
                c.getString(c.getColumnIndexOrThrow("asignado_nombre")),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAR_FECHA_INICIO)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAR_FECHA_LIMITE)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAR_OBSERVACIONES)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAR_FECHA_REGISTRO))
        );
    }
}
