package com.ecogestion.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecogestion.app.R;
import com.ecogestion.app.adapters.UsuarioAdapter;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.UsuarioDAO;
import com.ecogestion.app.models.Usuario;
import com.ecogestion.app.utils.RolHelper;
import com.ecogestion.app.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class UsuariosActivity extends AppCompatActivity implements UsuarioAdapter.OnUsuarioListener {

    private static final int REQUEST_FORM = 1;

    private RecyclerView recyclerUsuarios;
    private View layoutVacio;
    private UsuarioDAO usuarioDAO;
    private UsuarioAdapter adapter;
    private List<Usuario> listaUsuarios;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        usuarioDAO = new UsuarioDAO(DatabaseHelper.getInstance(this));
        sessionManager = new SessionManager(this);

        if (!RolHelper.puedeVerMenuUsuarios(sessionManager.getRol())) {
            Toast.makeText(this, "Acceso restringido: se requiere rol ADMIN", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerUsuarios = findViewById(R.id.recyclerUsuarios);
        layoutVacio      = findViewById(R.id.layoutVacio);

        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAgregarUsuario);
        fab.setOnClickListener(v -> abrirFormulario(null));

        cargarUsuarios();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        listaUsuarios = usuarioDAO.obtenerTodos();

        if (listaUsuarios.isEmpty()) {
            recyclerUsuarios.setVisibility(View.GONE);
            layoutVacio.setVisibility(View.VISIBLE);
        } else {
            recyclerUsuarios.setVisibility(View.VISIBLE);
            layoutVacio.setVisibility(View.GONE);
            adapter = new UsuarioAdapter(listaUsuarios, this);
            recyclerUsuarios.setAdapter(adapter);
        }
    }

    private void abrirFormulario(Usuario usuario) {
        Intent intent = new Intent(this, FormUsuarioActivity.class);
        if (usuario != null) {
            intent.putExtra(FormUsuarioActivity.EXTRA_USUARIO_ID, usuario.getId());
        }
        startActivityForResult(intent, REQUEST_FORM);
    }

    @Override
    public void onEditar(Usuario usuario) {
        abrirFormulario(usuario);
    }

    @Override
    public void onEliminar(Usuario usuario) {
        // No se puede eliminar al usuario que tiene la sesión activa
        if (usuario.getId() == sessionManager.getUsuarioId()) {
            new AlertDialog.Builder(this)
                    .setTitle("Operación no permitida")
                    .setMessage("No podés eliminar tu propia cuenta.")
                    .setPositiveButton("Entendido", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar usuario")
                .setMessage("¿Estás seguro de que querés eliminar a "
                        + usuario.getNombre() + " " + usuario.getApellido() + "?\n"
                        + "Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    usuarioDAO.eliminar(usuario.getId());
                    usuarioDAO.registrarAcceso(
                            sessionManager.getUsuarioId(),
                            "ELIMINAR_USUARIO",
                            "Usuario eliminado: " + usuario.getNombreUsuario()
                    );
                    cargarUsuarios();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FORM && resultCode == RESULT_OK) {
            cargarUsuarios();
        }
    }
}
