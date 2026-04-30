package com.ecogestion.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ecogestion.db";
    private static final int DATABASE_VERSION = 1;

    // Tabla usuarios
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COL_ID = "id";
    public static final String COL_NOMBRE_USUARIO = "nombre_usuario";
    public static final String COL_CONTRASENA = "contrasena";
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_APELLIDO = "apellido";
    public static final String COL_EMAIL = "email";
    public static final String COL_ROL = "rol";
    public static final String COL_ACTIVO = "activo";

    // Tabla auditoría
    public static final String TABLE_AUDITORIA = "auditoria";
    public static final String COL_AUD_ID = "id";
    public static final String COL_AUD_USUARIO_ID = "usuario_id";
    public static final String COL_AUD_ACCION = "accion";
    public static final String COL_AUD_DETALLE = "detalle";
    public static final String COL_AUD_FECHA = "fecha";

    private static DatabaseHelper instancia;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instancia == null) {
            instancia = new DatabaseHelper(context.getApplicationContext());
        }
        return instancia;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(crearTablaUsuarios());
        db.execSQL(crearTablaAuditoria());
        insertarUsuarioAdmin(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUDITORIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }

    private String crearTablaUsuarios() {
        return "CREATE TABLE " + TABLE_USUARIOS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NOMBRE_USUARIO + " TEXT NOT NULL UNIQUE, "
                + COL_CONTRASENA + " TEXT NOT NULL, "
                + COL_NOMBRE + " TEXT NOT NULL, "
                + COL_APELLIDO + " TEXT NOT NULL, "
                + COL_EMAIL + " TEXT, "
                + COL_ROL + " TEXT NOT NULL DEFAULT 'OPERADOR', "
                + COL_ACTIVO + " INTEGER NOT NULL DEFAULT 1"
                + ")";
    }

    private String crearTablaAuditoria() {
        return "CREATE TABLE " + TABLE_AUDITORIA + " ("
                + COL_AUD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_AUD_USUARIO_ID + " INTEGER NOT NULL, "
                + COL_AUD_ACCION + " TEXT NOT NULL, "
                + COL_AUD_DETALLE + " TEXT, "
                + COL_AUD_FECHA + " TEXT NOT NULL DEFAULT (datetime('now','localtime')), "
                + "FOREIGN KEY (" + COL_AUD_USUARIO_ID + ") REFERENCES "
                + TABLE_USUARIOS + "(" + COL_ID + ")"
                + ")";
    }

    private void insertarUsuarioAdmin(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COL_NOMBRE_USUARIO, "admin");
        values.put(COL_CONTRASENA, "admin123");
        values.put(COL_NOMBRE, "Administrador");
        values.put(COL_APELLIDO, "Sistema");
        values.put(COL_EMAIL, "admin@ambiente.cba.gov.ar");
        values.put(COL_ROL, "ADMIN");
        values.put(COL_ACTIVO, 1);
        db.insert(TABLE_USUARIOS, null, values);
    }
}
