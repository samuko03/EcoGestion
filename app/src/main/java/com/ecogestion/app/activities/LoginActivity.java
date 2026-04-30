package com.ecogestion.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ecogestion.app.MainActivity;
import com.ecogestion.app.R;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.UsuarioDAO;
import com.ecogestion.app.models.Usuario;
import com.ecogestion.app.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsuario, tilContrasena;
    private TextInputEditText etUsuario, etContrasena;
    private MaterialButton btnIngresar;

    private UsuarioDAO usuarioDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (sessionManager.estaLogueado()) {
            irAlMenu();
            return;
        }

        setContentView(R.layout.activity_login);
        inicializarVistas();
        inicializarDatos();

        btnIngresar.setOnClickListener(v -> intentarLogin());
    }

    private void inicializarVistas() {
        tilUsuario = findViewById(R.id.tilUsuario);
        tilContrasena = findViewById(R.id.tilContrasena);
        etUsuario = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        btnIngresar = findViewById(R.id.btnIngresar);
    }

    private void inicializarDatos() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        usuarioDAO = new UsuarioDAO(dbHelper);
    }

    private void intentarLogin() {
        tilUsuario.setError(null);
        tilContrasena.setError(null);

        String nombreUsuario = etUsuario.getText() != null
                ? etUsuario.getText().toString().trim() : "";
        String contrasena = etContrasena.getText() != null
                ? etContrasena.getText().toString().trim() : "";

        if (TextUtils.isEmpty(nombreUsuario)) {
            tilUsuario.setError(getString(R.string.error_campo_vacio));
            etUsuario.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(contrasena)) {
            tilContrasena.setError(getString(R.string.error_campo_vacio));
            etContrasena.requestFocus();
            return;
        }

        btnIngresar.setEnabled(false);

        Usuario usuario = usuarioDAO.buscarPorCredenciales(nombreUsuario, contrasena);

        if (usuario == null) {
            btnIngresar.setEnabled(true);
            tilContrasena.setError(getString(R.string.error_credenciales));
            registrarIntentoFallido(nombreUsuario);
            return;
        }

        if (!usuario.isActivo()) {
            btnIngresar.setEnabled(true);
            Toast.makeText(this, R.string.error_usuario_inactivo, Toast.LENGTH_LONG).show();
            return;
        }

        sessionManager.guardarSesion(usuario);
        usuarioDAO.registrarAcceso(usuario.getId(), "LOGIN", "Inicio de sesión exitoso");
        irAlMenu();
    }

    private void registrarIntentoFallido(String nombreUsuario) {
        // Se registra con ID 0 ya que el usuario no fue autenticado
        usuarioDAO.registrarAcceso(0, "LOGIN_FALLIDO",
                "Intento fallido para usuario: " + nombreUsuario);
    }

    private void irAlMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
