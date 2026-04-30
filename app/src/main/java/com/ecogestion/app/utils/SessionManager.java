package com.ecogestion.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ecogestion.app.models.Usuario;

public class SessionManager {

    private static final String PREFS_NAME = "ecogestion_session";
    private static final String KEY_ID = "usuario_id";
    private static final String KEY_NOMBRE_USUARIO = "nombre_usuario";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_APELLIDO = "apellido";
    private static final String KEY_ROL = "rol";
    private static final String KEY_LOGUEADO = "logueado";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void guardarSesion(Usuario usuario) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_ID, usuario.getId());
        editor.putString(KEY_NOMBRE_USUARIO, usuario.getNombreUsuario());
        editor.putString(KEY_NOMBRE, usuario.getNombre());
        editor.putString(KEY_APELLIDO, usuario.getApellido());
        editor.putString(KEY_ROL, usuario.getRol());
        editor.putBoolean(KEY_LOGUEADO, true);
        editor.apply();
    }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }

    public boolean estaLogueado() {
        return prefs.getBoolean(KEY_LOGUEADO, false);
    }

    public int getUsuarioId() { return prefs.getInt(KEY_ID, -1); }
    public String getNombreUsuario() { return prefs.getString(KEY_NOMBRE_USUARIO, ""); }
    public String getNombre() { return prefs.getString(KEY_NOMBRE, ""); }
    public String getApellido() { return prefs.getString(KEY_APELLIDO, ""); }
    public String getRol() { return prefs.getString(KEY_ROL, ""); }

    public String getNombreCompleto() {
        return getNombre() + " " + getApellido();
    }
}
