package com.ecogestion.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.models.Usuario;

public class UsuarioDAO {

    private final DatabaseHelper dbHelper;

    public UsuarioDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public Usuario buscarPorCredenciales(String nombreUsuario, String contrasena) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Usuario usuario = null;

        String[] columnas = {
                DatabaseHelper.COL_ID,
                DatabaseHelper.COL_NOMBRE_USUARIO,
                DatabaseHelper.COL_CONTRASENA,
                DatabaseHelper.COL_NOMBRE,
                DatabaseHelper.COL_APELLIDO,
                DatabaseHelper.COL_EMAIL,
                DatabaseHelper.COL_ROL,
                DatabaseHelper.COL_ACTIVO
        };

        String selection = DatabaseHelper.COL_NOMBRE_USUARIO + " = ? AND "
                + DatabaseHelper.COL_CONTRASENA + " = ?";
        String[] selectionArgs = { nombreUsuario, contrasena };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USUARIOS,
                columnas,
                selection,
                selectionArgs,
                null, null, null
        );

        if (cursor.moveToFirst()) {
            usuario = cursorToUsuario(cursor);
        }

        cursor.close();
        return usuario;
    }

    public void registrarAcceso(int usuarioId, String accion, String detalle) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_AUD_USUARIO_ID, usuarioId);
        values.put(DatabaseHelper.COL_AUD_ACCION, accion);
        values.put(DatabaseHelper.COL_AUD_DETALLE, detalle);
        db.insert(DatabaseHelper.TABLE_AUDITORIA, null, values);
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
