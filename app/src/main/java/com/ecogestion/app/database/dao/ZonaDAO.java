package com.ecogestion.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.models.Zona;

import java.util.ArrayList;
import java.util.List;

public class ZonaDAO {

    private final DatabaseHelper dbHelper;

    public ZonaDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Zona zona) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ZONA_NOMBRE, zona.getNombre());
        values.put(DatabaseHelper.COL_ZONA_DEPARTAMENTO, zona.getDepartamento());
        values.put(DatabaseHelper.COL_ZONA_LOCALIDAD, zona.getLocalidad());
        values.put(DatabaseHelper.COL_ZONA_LATITUD, zona.getLatitud());
        values.put(DatabaseHelper.COL_ZONA_LONGITUD, zona.getLongitud());
        values.put(DatabaseHelper.COL_ZONA_ESTADO, zona.getEstado());
        values.put(DatabaseHelper.COL_ZONA_RESPONSABLE_ID, zona.getResponsableId());
        return db.insert(DatabaseHelper.TABLE_ZONAS, null, values);
    }

    public int actualizar(Zona zona) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ZONA_NOMBRE, zona.getNombre());
        values.put(DatabaseHelper.COL_ZONA_DEPARTAMENTO, zona.getDepartamento());
        values.put(DatabaseHelper.COL_ZONA_LOCALIDAD, zona.getLocalidad());
        values.put(DatabaseHelper.COL_ZONA_LATITUD, zona.getLatitud());
        values.put(DatabaseHelper.COL_ZONA_LONGITUD, zona.getLongitud());
        values.put(DatabaseHelper.COL_ZONA_ESTADO, zona.getEstado());
        values.put(DatabaseHelper.COL_ZONA_RESPONSABLE_ID, zona.getResponsableId());
        return db.update(DatabaseHelper.TABLE_ZONAS, values,
                DatabaseHelper.COL_ZONA_ID + " = ?",
                new String[]{String.valueOf(zona.getId())});
    }

    public int eliminar(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_ZONAS,
                DatabaseHelper.COL_ZONA_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public List<Zona> obtenerTodas() {
        List<Zona> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT z.*, u." + DatabaseHelper.COL_NOMBRE + " || ' ' || u." + DatabaseHelper.COL_APELLIDO
                + " AS responsable_nombre FROM " + DatabaseHelper.TABLE_ZONAS + " z"
                + " LEFT JOIN " + DatabaseHelper.TABLE_USUARIOS + " u ON z."
                + DatabaseHelper.COL_ZONA_RESPONSABLE_ID + " = u." + DatabaseHelper.COL_ID
                + " ORDER BY z." + DatabaseHelper.COL_ZONA_NOMBRE + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            lista.add(cursorToZona(cursor));
        }
        cursor.close();
        return lista;
    }

    public List<Zona> obtenerPorEstado(String estado) {
        List<Zona> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ZONAS, null,
                DatabaseHelper.COL_ZONA_ESTADO + " = ?",
                new String[]{estado}, null, null,
                DatabaseHelper.COL_ZONA_NOMBRE + " ASC");
        while (cursor.moveToNext()) {
            lista.add(cursorToZona(cursor));
        }
        cursor.close();
        return lista;
    }

    public Zona obtenerPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ZONAS, null,
                DatabaseHelper.COL_ZONA_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Zona zona = null;
        if (cursor.moveToFirst()) {
            zona = cursorToZona(cursor);
        }
        cursor.close();
        return zona;
    }

    public int contarTodas() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ZONAS, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    private Zona cursorToZona(Cursor cursor) {
        Zona zona = new Zona();
        zona.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ZONA_ID)));
        zona.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ZONA_NOMBRE)));
        zona.setDepartamento(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ZONA_DEPARTAMENTO)));
        zona.setLocalidad(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ZONA_LOCALIDAD)));
        zona.setLatitud(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ZONA_LATITUD)));
        zona.setLongitud(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ZONA_LONGITUD)));
        zona.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ZONA_ESTADO)));
        zona.setResponsableId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ZONA_RESPONSABLE_ID)));
        zona.setFechaRegistro(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ZONA_FECHA)));
        int colResponsable = cursor.getColumnIndex("responsable_nombre");
        if (colResponsable != -1) zona.setResponsableNombre(cursor.getString(colResponsable));
        return zona;
    }
}
