package com.ecogestion.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.models.Usuario;

import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private final DatabaseHelper dbHelper;

    public UsuarioDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ── Autenticación ──────────────────────────────────────────
    public Usuario buscarPorCredenciales(String nombreUsuario, String contrasena) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_NOMBRE_USUARIO + " = ? AND "
                + DatabaseHelper.COL_CONTRASENA + " = ?";
        Cursor cursor = db.query(DatabaseHelper.TABLE_USUARIOS, null,
                selection, new String[]{nombreUsuario, contrasena},
                null, null, null);
        Usuario usuario = null;
        if (cursor.moveToFirst()) usuario = cursorToUsuario(cursor);
        cursor.close();
        return usuario;
    }

    // ── CRUD ───────────────────────────────────────────────────
    public long insertar(Usuario usuario) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(DatabaseHelper.TABLE_USUARIOS, null, toContentValues(usuario));
    }

    public int actualizar(Usuario usuario) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(DatabaseHelper.TABLE_USUARIOS, toContentValues(usuario),
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(usuario.getId())});
    }

    public int actualizarContrasena(int id, String nuevaContrasena) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_CONTRASENA, nuevaContrasena);
        return db.update(DatabaseHelper.TABLE_USUARIOS, values,
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public int eliminar(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_USUARIOS,
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // ── Consultas ──────────────────────────────────────────────
    public List<Usuario> obtenerTodos() {
        List<Usuario> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USUARIOS, null,
                null, null, null, null,
                DatabaseHelper.COL_APELLIDO + " ASC");
        while (cursor.moveToNext()) lista.add(cursorToUsuario(cursor));
        cursor.close();
        return lista;
    }

    public Usuario obtenerPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USUARIOS, null,
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Usuario usuario = null;
        if (cursor.moveToFirst()) usuario = cursorToUsuario(cursor);
        cursor.close();
        return usuario;
    }

    public boolean existeNombreUsuario(String nombreUsuario, int excludeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USUARIOS,
                new String[]{DatabaseHelper.COL_ID},
                DatabaseHelper.COL_NOMBRE_USUARIO + " = ? AND " + DatabaseHelper.COL_ID + " != ?",
                new String[]{nombreUsuario, String.valueOf(excludeId)},
                null, null, null);
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    // ── Auditoría ──────────────────────────────────────────────
    public void registrarAcceso(int usuarioId, String accion, String detalle) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_AUD_USUARIO_ID, usuarioId);
        values.put(DatabaseHelper.COL_AUD_ACCION, accion);
        values.put(DatabaseHelper.COL_AUD_DETALLE, detalle);
        db.insert(DatabaseHelper.TABLE_AUDITORIA, null, values);
    }

    // ── Helpers ────────────────────────────────────────────────
    private ContentValues toContentValues(Usuario u) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NOMBRE_USUARIO, u.getNombreUsuario());
        values.put(DatabaseHelper.COL_CONTRASENA, u.getContrasena());
        values.put(DatabaseHelper.COL_NOMBRE, u.getNombre());
        values.put(DatabaseHelper.COL_APELLIDO, u.getApellido());
        values.put(DatabaseHelper.COL_EMAIL, u.getEmail());
        values.put(DatabaseHelper.COL_ROL, u.getRol());
        values.put(DatabaseHelper.COL_ACTIVO, u.isActivo() ? 1 : 0);
        return values;
    }

    private Usuario cursorToUsuario(Cursor cursor) {
        return new Usuario(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOMBRE_USUARIO)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTRASENA)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOMBRE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_APELLIDO)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ROL)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACTIVO)) == 1
        );
    }
}
