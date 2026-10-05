package com.ecogestion.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.ecogestion.app.MainActivity;
import com.ecogestion.app.R;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.UsuarioDAO;
import com.ecogestion.app.models.Usuario;
import com.ecogestion.app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    // Orden de visualización de roles
    private static final String[] GRUPOS = {"ADMIN", "SUPERVISOR", "OPERADOR", "INSPECTOR", "COORDINADOR"};

    private LinearLayout layoutUsuarios;
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

        layoutUsuarios = findViewById(R.id.layoutUsuarios);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        usuarioDAO = new UsuarioDAO(dbHelper);

        cargarUsuarios();
    }

    // ── Carga usuarios agrupados por rol ───────────────────────
    private void cargarUsuarios() {
        List<Usuario> todos = usuarioDAO.obtenerTodos();
        layoutUsuarios.removeAllViews();

        // Filtrar solo activos
        List<Usuario> activos = new ArrayList<>();
        for (Usuario u : todos) {
            if (u.isActivo()) activos.add(u);
        }

        if (activos.isEmpty()) {
            agregarMensajeVacio();
            return;
        }

        boolean algúnGrupoMostrado = false;

        for (String rol : GRUPOS) {
            List<Usuario> grupo = filtrarPorRol(activos, rol);
            if (grupo.isEmpty()) continue;

            // Separador entre grupos (excepto el primero)
            if (algúnGrupoMostrado) {
                agregarSeparador();
            }

            // Encabezado del grupo
            agregarEncabezadoGrupo(etiquetaGrupo(rol));

            // Tarjetas del grupo
            for (Usuario usuario : grupo) {
                agregarTarjetaUsuario(usuario, colorAvatar(rol));
            }

            algúnGrupoMostrado = true;
        }
    }

    // ── Agrega encabezado de sección ───────────────────────────
    private void agregarEncabezadoGrupo(String titulo) {
        TextView tv = new TextView(this);
        tv.setText(titulo);
        tv.setTextSize(11f);
        tv.setTextColor(getResources().getColor(R.color.gray_hint, null));
        tv.setAllCaps(true);
        tv.setLetterSpacing(0.08f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(6));
        tv.setLayoutParams(params);

        layoutUsuarios.addView(tv);
    }

    // ── Línea divisoria entre grupos ──────────────────────────
    private void agregarSeparador() {
        View linea = new View(this);
        linea.setBackgroundColor(getResources().getColor(R.color.gray_divider, null));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
        params.setMargins(dpToPx(8), dpToPx(10), dpToPx(8), dpToPx(4));
        linea.setLayoutParams(params);

        layoutUsuarios.addView(linea);
    }

    // ── Infla y configura la tarjeta de un usuario ─────────────
    private void agregarTarjetaUsuario(Usuario usuario, int colorAvatar) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View card = inflater.inflate(R.layout.item_usuario_login, layoutUsuarios, false);

        // Color del avatar según rol
        CardView cardAvatar = card.findViewById(R.id.cardAvatar);
        cardAvatar.setCardBackgroundColor(colorAvatar);

        // Inicial del nombre
        TextView tvInicial = card.findViewById(R.id.tvInicial);
        String inicial = usuario.getNombre() != null && !usuario.getNombre().isEmpty()
                ? String.valueOf(usuario.getNombre().charAt(0)).toUpperCase()
                : "?";
        tvInicial.setText(inicial);

        // Nombre completo
        TextView tvNombre = card.findViewById(R.id.tvNombreCompleto);
        tvNombre.setText(usuario.getNombreCompleto());

        // Rol formateado
        TextView tvRol = card.findViewById(R.id.tvRol);
        tvRol.setText(formatearRol(usuario.getRol()));

        // Clic → diálogo de contraseña
        LinearLayout layoutCard = card.findViewById(R.id.layoutCard);
        layoutCard.setOnClickListener(v -> mostrarDialogContrasena(usuario));

        layoutUsuarios.addView(card);
    }

    // ── Mensaje cuando no hay nadie registrado ─────────────────
    private void agregarMensajeVacio() {
        TextView tv = new TextView(this);
        tv.setText("No hay usuarios registrados en el sistema.");
        tv.setTextColor(getResources().getColor(R.color.gray_hint, null));
        tv.setTextSize(13f);
        tv.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dpToPx(40), 0, 0);
        tv.setLayoutParams(params);
        layoutUsuarios.addView(tv);
    }

    // ── Diálogo de contraseña ──────────────────────────────────
    private void mostrarDialogContrasena(Usuario usuario) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_password, null);

        TextView tvNombre = dialogView.findViewById(R.id.tvDialogNombre);
        tvNombre.setText("Hola, " + usuario.getNombre() + " " + usuario.getApellido());

        TextInputLayout tilPassword = dialogView.findViewById(R.id.tilPassword);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Ingresar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String contrasena = etPassword.getText() != null
                        ? etPassword.getText().toString().trim() : "";

                if (contrasena.isEmpty()) {
                    tilPassword.setError("Ingresá tu contraseña");
                    etPassword.requestFocus();
                    return;
                }

                tilPassword.setError(null);
                autenticar(usuario, contrasena, dialog, tilPassword);
            });

            etPassword.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            });

            etPassword.requestFocus();
        });

        dialog.show();
    }

    // ── Verifica credenciales ──────────────────────────────────
    private void autenticar(Usuario usuario, String contrasena,
                            AlertDialog dialog, TextInputLayout tilPassword) {
        Usuario autenticado = usuarioDAO.buscarPorCredenciales(
                usuario.getNombreUsuario(), contrasena);

        if (autenticado == null) {
            tilPassword.setError("Contraseña incorrecta");
            usuarioDAO.registrarAcceso(0, "LOGIN_FALLIDO",
                    "Contraseña incorrecta para: " + usuario.getNombreUsuario());
            return;
        }

        dialog.dismiss();
        sessionManager.guardarSesion(autenticado);
        usuarioDAO.registrarAcceso(autenticado.getId(), "LOGIN", "Inicio de sesión exitoso");
        irAlMenu();
    }

    // ── Navegación ─────────────────────────────────────────────
    private void irAlMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ── Helpers ────────────────────────────────────────────────

    private List<Usuario> filtrarPorRol(List<Usuario> lista, String rol) {
        List<Usuario> resultado = new ArrayList<>();
        for (Usuario u : lista) {
            if (rol.equals(u.getRol())) resultado.add(u);
        }
        return resultado;
    }

    private String etiquetaGrupo(String rol) {
        switch (rol) {
            case "ADMIN":        return "Administración";
            case "SUPERVISOR":   return "Supervisores";
            case "OPERADOR":     return "Operadores";
            case "INSPECTOR":    return "Inspectores";
            case "COORDINADOR":  return "Coordinadores";
            default:             return rol;
        }
    }

    private String formatearRol(String rol) {
        switch (rol) {
            case "ADMIN":        return "Administrador";
            case "SUPERVISOR":   return "Supervisor";
            case "OPERADOR":     return "Operador";
            case "INSPECTOR":    return "Inspector";
            case "COORDINADOR":  return "Coordinador";
            default:             return rol != null ? rol : "";
        }
    }

    private int colorAvatar(String rol) {
        switch (rol) {
            case "ADMIN":        return getResources().getColor(R.color.inst_blue, null);
            case "SUPERVISOR":   return getResources().getColor(R.color.teal_accent, null);
            case "OPERADOR":     return getResources().getColor(R.color.green_dark, null);
            case "INSPECTOR":    return getResources().getColor(R.color.inst_yellow, null);
            case "COORDINADOR":  return getResources().getColor(R.color.green_primary, null);
            default:             return getResources().getColor(R.color.gray_hint, null);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
