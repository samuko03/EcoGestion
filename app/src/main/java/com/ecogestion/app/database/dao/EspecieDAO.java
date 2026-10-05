package com.ecogestion.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.models.Especie;

import java.util.ArrayList;
import java.util.List;

public class EspecieDAO {

    private final DatabaseHelper dbHelper;

    public EspecieDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Especie especie) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(especie);
        return db.insert(DatabaseHelper.TABLE_ESPECIES, null, values);
    }

    public int actualizar(Especie especie) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(especie);
        return db.update(DatabaseHelper.TABLE_ESPECIES, values,
                DatabaseHelper.COL_ESP_ID + " = ?",
                new String[]{String.valueOf(especie.getId())});
    }

    public int actualizarStock(int id, int nuevaCantidad) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ESP_CANTIDAD, nuevaCantidad);
        return db.update(DatabaseHelper.TABLE_ESPECIES, values,
                DatabaseHelper.COL_ESP_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public int eliminar(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_ESPECIES,
                DatabaseHelper.COL_ESP_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public List<Especie> obtenerTodas() {
        List<Especie> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ESPECIES, null,
                null, null, null, null,
                DatabaseHelper.COL_ESP_NOMBRE + " ASC");
        while (cursor.moveToNext()) lista.add(cursorToEspecie(cursor));
        cursor.close();
        return lista;
    }

    public List<Especie> obtenerPorEcorregion(String ecorregion) {
        List<Especie> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ESPECIES, null,
                DatabaseHelper.COL_ESP_ECORREGION + " = ?",
                new String[]{ecorregion}, null, null,
                DatabaseHelper.COL_ESP_NOMBRE + " ASC");
        while (cursor.moveToNext()) lista.add(cursorToEspecie(cursor));
        cursor.close();
        return lista;
    }

    public Especie obtenerPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ESPECIES, null,
                DatabaseHelper.COL_ESP_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Especie especie = null;
        if (cursor.moveToFirst()) especie = cursorToEspecie(cursor);
        cursor.close();
        return especie;
    }

    public int contarTodas() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ESPECIES, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    private ContentValues toContentValues(Especie e) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ESP_NOMBRE, e.getNombre());
        values.put(DatabaseHelper.COL_ESP_NOMBRE_CIENTIFICO, e.getNombreCientifico());
        values.put(DatabaseHelper.COL_ESP_ECORREGION, e.getEcorregion());
        values.put(DatabaseHelper.COL_ESP_CANTIDAD, e.getCantidadDisponible());
        values.put(DatabaseHelper.COL_ESP_DESCRIPCION, e.getDescripcion());
        return values;
    }

    private Especie cursorToEspecie(Cursor cursor) {
        Especie e = new Especie();
        e.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ESP_ID)));
        e.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ESP_NOMBRE)));
        e.setNombreCientifico(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ESP_NOMBRE_CIENTIFICO)));
        e.setEcorregion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ESP_ECORREGION)));
        e.setCantidadDisponible(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ESP_CANTIDAD)));
        e.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ESP_DESCRIPCION)));
        e.setFechaRegistro(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ESP_FECHA)));
        return e;
    }
}
