package com.ecogestion.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.models.Plantacion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlantacionDAO {

    private final DatabaseHelper dbHelper;

    public PlantacionDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ── CRUD ───────────────────────────────────────────────────
    public long insertar(Plantacion p) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(DatabaseHelper.TABLE_PLANTACIONES, null, toContentValues(p));
    }

    public int actualizar(Plantacion p) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(DatabaseHelper.TABLE_PLANTACIONES, toContentValues(p),
                DatabaseHelper.COL_PL_ID + " = ?",
                new String[]{String.valueOf(p.getId())});
    }

    public int eliminar(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_PLANTACIONES,
                DatabaseHelper.COL_PL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // ── Consultas ──────────────────────────────────────────────
    public List<Plantacion> obtenerTodas() {
        List<Plantacion> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryConJoins(null), null);
        while (cursor.moveToNext()) lista.add(cursorToPlantacion(cursor));
        cursor.close();
        return lista;
    }

    public List<Plantacion> obtenerPorEstado(String estado) {
        List<Plantacion> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String where = "p." + DatabaseHelper.COL_PL_ESTADO + " = ?";
        Cursor cursor = db.rawQuery(queryConJoins(where), new String[]{estado});
        while (cursor.moveToNext()) lista.add(cursorToPlantacion(cursor));
        cursor.close();
        return lista;
    }

    public Plantacion obtenerPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String where = "p." + DatabaseHelper.COL_PL_ID + " = ?";
        Cursor cursor = db.rawQuery(queryConJoins(where), new String[]{String.valueOf(id)});
        Plantacion p = null;
        if (cursor.moveToFirst()) p = cursorToPlantacion(cursor);
        cursor.close();
        return p;
    }

    /**
     * Devuelve plantaciones aplicando filtros opcionales.
     * zonaId / especieId = 0 → sin filtro
     * fechaDesde / fechaHasta = null o vacío → sin filtro de fecha
     * Fechas en formato dd/MM/yyyy
     */
    public List<Plantacion> obtenerFiltradas(int zonaId, int especieId,
                                             String fechaDesde, String fechaHasta) {
        // Armar WHERE para zona y especie (SQL)
        StringBuilder where = new StringBuilder();
        List<String> args   = new ArrayList<>();

        if (zonaId > 0) {
            where.append("p.").append(DatabaseHelper.COL_PL_ZONA_ID).append(" = ?");
            args.add(String.valueOf(zonaId));
        }
        if (especieId > 0) {
            if (where.length() > 0) where.append(" AND ");
            where.append("p.").append(DatabaseHelper.COL_PL_ESPECIE_ID).append(" = ?");
            args.add(String.valueOf(especieId));
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = queryConJoins(where.length() > 0 ? where.toString() : null);
        Cursor cursor = db.rawQuery(sql, args.isEmpty() ? null : args.toArray(new String[0]));

        List<Plantacion> todas = new ArrayList<>();
        while (cursor.moveToNext()) todas.add(cursorToPlantacion(cursor));
        cursor.close();

        // Filtrar por fecha en Java (fechas guardadas como dd/MM/yyyy)
        if ((fechaDesde == null || fechaDesde.isEmpty())
                && (fechaHasta == null || fechaHasta.isEmpty())) {
            return todas;
        }
        List<Plantacion> resultado = new ArrayList<>();
        for (Plantacion p : todas) {
            if (fechaDentroDeRango(p.getFechaPlantacion(), fechaDesde, fechaHasta)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    private boolean fechaDentroDeRango(String fecha, String desde, String hasta) {
        if (fecha == null || fecha.isEmpty()) return true;
        SimpleDateFormat FMT_FECHA = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "AR"));
        try {
            Date d = FMT_FECHA.parse(fecha);
            if (desde != null && !desde.isEmpty()) {
                Date dDesde = FMT_FECHA.parse(desde);
                if (d.before(dDesde)) return false;
            }
            if (hasta != null && !hasta.isEmpty()) {
                Date dHasta = FMT_FECHA.parse(hasta);
                if (d.after(dHasta)) return false;
            }
            return true;
        } catch (ParseException e) {
            return true;
        }
    }

    public int contarTodas() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PLANTACIONES, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // ── Helpers ────────────────────────────────────────────────
    private String queryConJoins(String whereClause) {
        String sql = "SELECT p.*, "
                + "z." + DatabaseHelper.COL_ZONA_NOMBRE + " AS zona_nombre, "
                + "e." + DatabaseHelper.COL_ESP_NOMBRE  + " AS especie_nombre, "
                + "(u." + DatabaseHelper.COL_NOMBRE + " || ' ' || u." + DatabaseHelper.COL_APELLIDO + ") AS responsable_nombre "
                + "FROM " + DatabaseHelper.TABLE_PLANTACIONES + " p "
                + "LEFT JOIN " + DatabaseHelper.TABLE_ZONAS    + " z ON p." + DatabaseHelper.COL_PL_ZONA_ID    + " = z." + DatabaseHelper.COL_ZONA_ID   + " "
                + "LEFT JOIN " + DatabaseHelper.TABLE_ESPECIES + " e ON p." + DatabaseHelper.COL_PL_ESPECIE_ID + " = e." + DatabaseHelper.COL_ESP_ID    + " "
                + "LEFT JOIN " + DatabaseHelper.TABLE_USUARIOS + " u ON p." + DatabaseHelper.COL_PL_RESPONSABLE_ID + " = u." + DatabaseHelper.COL_ID;
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        sql += " ORDER BY p." + DatabaseHelper.COL_PL_FECHA_REGISTRO + " DESC";
        return sql;
    }

    private ContentValues toContentValues(Plantacion p) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_PL_NOMBRE,           p.getNombre());
        cv.put(DatabaseHelper.COL_PL_ZONA_ID,          p.getZonaId());
        cv.put(DatabaseHelper.COL_PL_ESPECIE_ID,       p.getEspecieId());
        cv.put(DatabaseHelper.COL_PL_CANTIDAD,         p.getCantidadArboles());
        cv.put(DatabaseHelper.COL_PL_FECHA_PLANTACION, p.getFechaPlantacion());
        cv.put(DatabaseHelper.COL_PL_ESTADO,           p.getEstado());
        cv.put(DatabaseHelper.COL_PL_RESPONSABLE_ID,   p.getResponsableId() > 0 ? p.getResponsableId() : null);
        cv.put(DatabaseHelper.COL_PL_OBSERVACIONES,    p.getObservaciones());
        if (p.tieneUbicacion()) {
            cv.put(DatabaseHelper.COL_PL_LATITUD,  p.getLatitud());
            cv.put(DatabaseHelper.COL_PL_LONGITUD, p.getLongitud());
        }
        return cv;
    }

    private Plantacion cursorToPlantacion(Cursor c) {
        Plantacion p = new Plantacion(
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PL_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PL_NOMBRE)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PL_ZONA_ID)),
                c.getString(c.getColumnIndexOrThrow("zona_nombre")),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PL_ESPECIE_ID)),
                c.getString(c.getColumnIndexOrThrow("especie_nombre")),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PL_CANTIDAD)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PL_FECHA_PLANTACION)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PL_ESTADO)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PL_RESPONSABLE_ID)),
                c.getString(c.getColumnIndexOrThrow("responsable_nombre")),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PL_OBSERVACIONES)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PL_FECHA_REGISTRO))
        );
        // Latitud / Longitud (opcionales, agregados en versión 6)
        int latIdx = c.getColumnIndex(DatabaseHelper.COL_PL_LATITUD);
        int lngIdx = c.getColumnIndex(DatabaseHelper.COL_PL_LONGITUD);
        if (latIdx != -1 && !c.isNull(latIdx)) p.setLatitud(c.getDouble(latIdx));
        if (lngIdx != -1 && !c.isNull(lngIdx)) p.setLongitud(c.getDouble(lngIdx));
        return p;
    }
}
