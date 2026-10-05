package com.ecogestion.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.ecogestion.app.R;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.UsuarioDAO;
import com.ecogestion.app.models.Usuario;
import com.ecogestion.app.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class FormUsuarioActivity extends AppCompatActivity {

    public static final String EXTRA_USUARIO_ID = "usuario_id";

    private TextInputLayout tilNombre, tilApellido, tilNombreUsuario, tilContrasena;
    private TextInputEditText etNombre, etApellido, etEmail,
                               etNombreUsuario, etContrasena;
    private Spinner spinnerRol;
    private SwitchCompat switchActivo;

    private UsuarioDAO usuarioDAO;
    private SessionManager sessionManager;
    private Usuario usuarioEditando; // null = nuevo

    private static final String[] ROLES = {"OPERADOR", "SUPERVISOR", "INSPECTOR", "COORDINADOR", "ADMIN"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_usuario);

        usuarioDAO     = new UsuarioDAO(DatabaseHelper.getInstance(this));
        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Vistas
        tilNombre        = findViewById(R.id.tilNombre);
        tilApellido      = findViewById(R.id.tilApellido);
        tilNombreUsuario = findViewById(R.id.tilNombreUsuario);
        tilContrasena    = findViewById(R.id.tilContrasena);

        etNombre        = findViewById(R.id.etNombre);
        etApellido      = findViewById(R.id.etApellido);
        etEmail         = findViewById(R.id.etEmail);
        etNombreUsuario = findViewById(R.id.etNombreUsuario);
        etContrasena    = findViewById(R.id.etContrasena);

        spinnerRol  = findViewById(R.id.spinnerRol);
        switchActivo= findViewById(R.id.switchActivo);

        // Spinner de roles
        ArrayAdapter<String> rolAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, ROLES);
        rolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(rolAdapter);

        // ¿Editar o nuevo?
        int usuarioId = getIntent().getIntExtra(EXTRA_USUARIO_ID, -1);
        if (usuarioId != -1) {
            usuarioEditando = usuarioDAO.obtenerPorId(usuarioId);
            cargarDatos();
            toolbar.setTitle("Editar Usuario");
            // La contraseña es opcional al editar
            tilContrasena.setHint("Contraseña (dejar vacío para no cambiar)");
        } else {
            toolbar.setTitle("Nuevo Usuario");
        }

        MaterialButton btnGuardar = findViewById(R.id.btnGuardarUsuario);
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void cargarDatos() {
        etNombre.setText(usuarioEditando.getNombre());
        etApellido.setText(usuarioEditando.getApellido());
        etEmail.setText(usuarioEditando.getEmail());
        etNombreUsuario.setText(usuarioEditando.getNombreUsuario());
        switchActivo.setChecked(usuarioEditando.isActivo());

        // Seleccionar rol en spinner
        for (int i = 0; i < ROLES.length; i++) {
            if (ROLES[i].equals(usuarioEditando.getRol())) {
                spinnerRol.setSelection(i);
                break;
            }
        }
    }

    private void guardar() {
        if (!validar()) return;

        String nombre        = Objects.requireNonNull(etNombre.getText()).toString().trim();
        String apellido      = Objects.requireNonNull(etApellido.getText()).toString().trim();
        String email         = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String nombreUsuario = Objects.requireNonNull(etNombreUsuario.getText()).toString().trim();
        String contrasena    = Objects.requireNonNull(etContrasena.getText()).toString();
        String rol           = ROLES[spinnerRol.getSelectedItemPosition()];
        boolean activo       = switchActivo.isChecked();

        if (usuarioEditando == null) {
            // CREAR
            Usuario nuevo = new Usuario(0, nombreUsuario, contrasena,
                    nombre, apellido, email, rol, activo);
            long id = usuarioDAO.insertar(nuevo);

            if (id > 0) {
                usuarioDAO.registrarAcceso(
                        sessionManager.getUsuarioId(),
                        "CREAR_USUARIO",
                        "Usuario creado: " + nombreUsuario + " | Rol: " + rol
                );
                Toast.makeText(this, "Usuario creado correctamente", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error al crear el usuario", Toast.LENGTH_SHORT).show();
            }

        } else {
            // ACTUALIZAR datos generales
            usuarioEditando.setNombre(nombre);
            usuarioEditando.setApellido(apellido);
            usuarioEditando.setEmail(email);
            usuarioEditando.setNombreUsuario(nombreUsuario);
            usuarioEditando.setRol(rol);
            usuarioEditando.setActivo(activo);

            int rows = usuarioDAO.actualizar(usuarioEditando);

            // Si escribió nueva contraseña, actualizarla
            if (!contrasena.isEmpty() && rows > 0) {
                usuarioDAO.actualizarContrasena(usuarioEditando.getId(), contrasena);
            }

            if (rows > 0) {
                usuarioDAO.registrarAcceso(
                        sessionManager.getUsuarioId(),
                        "MODIFICAR_USUARIO",
                        "Usuario modificado: " + nombreUsuario + " | Rol: " + rol
                );
                Toast.makeText(this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error al actualizar el usuario", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validar() {
        boolean ok = true;

        String nombre = Objects.requireNonNull(etNombre.getText()).toString().trim();
        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError("El nombre es obligatorio");
            ok = false;
        } else {
            tilNombre.setError(null);
        }

        String apellido = Objects.requireNonNull(etApellido.getText()).toString().trim();
        if (TextUtils.isEmpty(apellido)) {
            tilApellido.setError("El apellido es obligatorio");
            ok = false;
        } else {
            tilApellido.setError(null);
        }

        String nombreUsuario = Objects.requireNonNull(etNombreUsuario.getText()).toString().trim();
        if (TextUtils.isEmpty(nombreUsuario)) {
            tilNombreUsuario.setError("El nombre de usuario es obligatorio");
            ok = false;
        } else if (nombreUsuario.contains(" ")) {
            tilNombreUsuario.setError("El nombre de usuario no puede tener espacios");
            ok = false;
        } else {
            int excludeId = (usuarioEditando != null) ? usuarioEditando.getId() : 0;
            if (usuarioDAO.existeNombreUsuario(nombreUsuario, excludeId)) {
                tilNombreUsuario.setError("Ese nombre de usuario ya está en uso");
                ok = false;
            } else {
                tilNombreUsuario.setError(null);
            }
        }

        String contrasena = Objects.requireNonNull(etContrasena.getText()).toString();
        if (usuarioEditando == null) {
            // Nuevo usuario: contraseña obligatoria
            if (TextUtils.isEmpty(contrasena)) {
                tilContrasena.setError("La contraseña es obligatoria");
                ok = false;
            } else if (contrasena.length() < 6) {
                tilContrasena.setError("Mínimo 6 caracteres");
                ok = false;
            } else {
                tilContrasena.setError(null);
            }
        } else {
            // Editar: solo validar si escribió algo
            if (!contrasena.isEmpty() && contrasena.length() < 6) {
                tilContrasena.setError("Mínimo 6 caracteres");
                ok = false;
            } else {
                tilContrasena.setError(null);
            }
        }

        return ok;
    }
}
