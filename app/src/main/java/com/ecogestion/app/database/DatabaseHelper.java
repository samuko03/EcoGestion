package com.ecogestion.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ecogestion.db";
    private static final int DATABASE_VERSION = 6;

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

    // Tabla especies
    public static final String TABLE_ESPECIES = "especies";
    public static final String COL_ESP_ID = "id";
    public static final String COL_ESP_NOMBRE = "nombre";
    public static final String COL_ESP_NOMBRE_CIENTIFICO = "nombre_cientifico";
    public static final String COL_ESP_ECORREGION = "ecorregion";
    public static final String COL_ESP_CANTIDAD = "cantidad_disponible";
    public static final String COL_ESP_DESCRIPCION = "descripcion";
    public static final String COL_ESP_FECHA = "fecha_registro";

    // Tabla tareas
    public static final String TABLE_TAREAS           = "tareas";
    public static final String COL_TAR_ID             = "id";
    public static final String COL_TAR_TITULO         = "titulo";
    public static final String COL_TAR_DESCRIPCION    = "descripcion";
    public static final String COL_TAR_TIPO           = "tipo";
    public static final String COL_TAR_ESTADO         = "estado";
    public static final String COL_TAR_PRIORIDAD      = "prioridad";
    public static final String COL_TAR_ZONA_ID        = "zona_id";
    public static final String COL_TAR_PLANTACION_ID  = "plantacion_id";
    public static final String COL_TAR_ASIGNADO_ID    = "asignado_id";
    public static final String COL_TAR_FECHA_INICIO   = "fecha_inicio";
    public static final String COL_TAR_FECHA_LIMITE   = "fecha_limite";
    public static final String COL_TAR_OBSERVACIONES  = "observaciones";
    public static final String COL_TAR_FECHA_REGISTRO = "fecha_registro";

    // Tabla plantaciones
    public static final String TABLE_PLANTACIONES     = "plantaciones";
    public static final String COL_PL_ID              = "id";
    public static final String COL_PL_NOMBRE          = "nombre";
    public static final String COL_PL_ZONA_ID         = "zona_id";
    public static final String COL_PL_ESPECIE_ID      = "especie_id";
    public static final String COL_PL_CANTIDAD        = "cantidad_arboles";
    public static final String COL_PL_FECHA_PLANTACION= "fecha_plantacion";
    public static final String COL_PL_ESTADO          = "estado";
    public static final String COL_PL_RESPONSABLE_ID  = "responsable_id";
    public static final String COL_PL_OBSERVACIONES   = "observaciones";
    public static final String COL_PL_LATITUD        = "latitud";
    public static final String COL_PL_LONGITUD       = "longitud";
    public static final String COL_PL_FECHA_REGISTRO  = "fecha_registro";

    // Tabla zonas
    public static final String TABLE_ZONAS = "zonas";
    public static final String COL_ZONA_ID = "id";
    public static final String COL_ZONA_NOMBRE = "nombre";
    public static final String COL_ZONA_DEPARTAMENTO = "departamento";
    public static final String COL_ZONA_LOCALIDAD = "localidad";
    public static final String COL_ZONA_LATITUD = "latitud";
    public static final String COL_ZONA_LONGITUD = "longitud";
    public static final String COL_ZONA_ESTADO = "estado";
    public static final String COL_ZONA_RESPONSABLE_ID = "responsable_id";
    public static final String COL_ZONA_FECHA = "fecha_registro";

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
        db.execSQL(crearTablaZonas());
        db.execSQL(crearTablaEspecies());
        db.execSQL(crearTablaPlantaciones());
        db.execSQL(crearTablaTareas());
        insertarUsuarioAdmin(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(crearTablaZonas());
        }
        if (oldVersion < 3) {
            db.execSQL(crearTablaEspecies());
        }
        if (oldVersion < 4) {
            db.execSQL(crearTablaPlantaciones());
        }
        if (oldVersion < 5) {
            db.execSQL(crearTablaTareas());
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + TABLE_PLANTACIONES
                    + " ADD COLUMN " + COL_PL_LATITUD + " REAL");
            db.execSQL("ALTER TABLE " + TABLE_PLANTACIONES
                    + " ADD COLUMN " + COL_PL_LONGITUD + " REAL");
        }
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

    private String crearTablaZonas() {
        return "CREATE TABLE " + TABLE_ZONAS + " ("
                + COL_ZONA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ZONA_NOMBRE + " TEXT NOT NULL, "
                + COL_ZONA_DEPARTAMENTO + " TEXT NOT NULL, "
                + COL_ZONA_LOCALIDAD + " TEXT NOT NULL, "
                + COL_ZONA_LATITUD + " TEXT, "
                + COL_ZONA_LONGITUD + " TEXT, "
                + COL_ZONA_ESTADO + " TEXT NOT NULL DEFAULT 'PENDIENTE', "
                + COL_ZONA_RESPONSABLE_ID + " INTEGER, "
                + COL_ZONA_FECHA + " TEXT NOT NULL DEFAULT (datetime('now','localtime')), "
                + "FOREIGN KEY (" + COL_ZONA_RESPONSABLE_ID + ") REFERENCES "
                + TABLE_USUARIOS + "(" + COL_ID + ")"
                + ")";
    }

    private String crearTablaEspecies() {
        return "CREATE TABLE " + TABLE_ESPECIES + " ("
                + COL_ESP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ESP_NOMBRE + " TEXT NOT NULL, "
                + COL_ESP_NOMBRE_CIENTIFICO + " TEXT, "
                + COL_ESP_ECORREGION + " TEXT, "
                + COL_ESP_CANTIDAD + " INTEGER NOT NULL DEFAULT 0, "
                + COL_ESP_DESCRIPCION + " TEXT, "
                + COL_ESP_FECHA + " TEXT NOT NULL DEFAULT (datetime('now','localtime'))"
                + ")";
    }

    private String crearTablaPlantaciones() {
        return "CREATE TABLE " + TABLE_PLANTACIONES + " ("
                + COL_PL_ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PL_NOMBRE           + " TEXT NOT NULL, "
                + COL_PL_ZONA_ID          + " INTEGER NOT NULL, "
                + COL_PL_ESPECIE_ID       + " INTEGER NOT NULL, "
                + COL_PL_CANTIDAD         + " INTEGER NOT NULL DEFAULT 0, "
                + COL_PL_FECHA_PLANTACION + " TEXT, "
                + COL_PL_ESTADO           + " TEXT NOT NULL DEFAULT 'PLANIFICADA', "
                + COL_PL_RESPONSABLE_ID   + " INTEGER, "
                + COL_PL_OBSERVACIONES    + " TEXT, "
                + COL_PL_LATITUD         + " REAL, "
                + COL_PL_LONGITUD        + " REAL, "
                + COL_PL_FECHA_REGISTRO   + " TEXT NOT NULL DEFAULT (datetime('now','localtime')), "
                + "FOREIGN KEY (" + COL_PL_ZONA_ID + ") REFERENCES " + TABLE_ZONAS + "(" + COL_ZONA_ID + "), "
                + "FOREIGN KEY (" + COL_PL_ESPECIE_ID + ") REFERENCES " + TABLE_ESPECIES + "(" + COL_ESP_ID + "), "
                + "FOREIGN KEY (" + COL_PL_RESPONSABLE_ID + ") REFERENCES " + TABLE_USUARIOS + "(" + COL_ID + ")"
                + ")";
    }

    private String crearTablaTareas() {
        return "CREATE TABLE " + TABLE_TAREAS + " ("
                + COL_TAR_ID             + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TAR_TITULO         + " TEXT NOT NULL, "
                + COL_TAR_DESCRIPCION    + " TEXT, "
                + COL_TAR_TIPO           + " TEXT NOT NULL DEFAULT 'OTRO', "
                + COL_TAR_ESTADO         + " TEXT NOT NULL DEFAULT 'PENDIENTE', "
                + COL_TAR_PRIORIDAD      + " TEXT NOT NULL DEFAULT 'MEDIA', "
                + COL_TAR_ZONA_ID        + " INTEGER, "
                + COL_TAR_PLANTACION_ID  + " INTEGER, "
                + COL_TAR_ASIGNADO_ID    + " INTEGER, "
                + COL_TAR_FECHA_INICIO   + " TEXT, "
                + COL_TAR_FECHA_LIMITE   + " TEXT, "
                + COL_TAR_OBSERVACIONES  + " TEXT, "
                + COL_TAR_FECHA_REGISTRO + " TEXT NOT NULL DEFAULT (datetime('now','localtime')), "
                + "FOREIGN KEY (" + COL_TAR_ZONA_ID + ") REFERENCES " + TABLE_ZONAS + "(" + COL_ZONA_ID + "), "
                + "FOREIGN KEY (" + COL_TAR_PLANTACION_ID + ") REFERENCES " + TABLE_PLANTACIONES + "(" + COL_PL_ID + "), "
                + "FOREIGN KEY (" + COL_TAR_ASIGNADO_ID + ") REFERENCES " + TABLE_USUARIOS + "(" + COL_ID + ")"
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
