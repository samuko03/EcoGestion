package com.ecogestion.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecogestion.app.R;
import com.ecogestion.app.adapters.TareaAdapter;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.TareaDAO;
import com.ecogestion.app.database.dao.UsuarioDAO;
import com.ecogestion.app.models.Tarea;
import com.ecogestion.app.utils.RolHelper;
import com.ecogestion.app.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class TareasActivity extends AppCompatActivity
        implements TareaAdapter.OnTareaListener {

    private static final int REQUEST_FORM = 1;

    private RecyclerView recyclerTareas;
    private View layoutVacio;
    private TareaDAO tareaDAO;
    private UsuarioDAO usuarioDAO;
    private SessionManager sessionManager;
    private List<Tarea> listaTareas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tareas);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        tareaDAO       = new TareaDAO(db);
        usuarioDAO     = new UsuarioDAO(db);
        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerTareas = findViewById(R.id.recyclerTareas);
        layoutVacio    = findViewById(R.id.layoutVacio);
        recyclerTareas.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAgregarTarea);
        if (RolHelper.puedeCrearTareas(sessionManager.getRol())) {
            fab.setOnClickListener(v -> abrirFormulario(null));
        } else {
            fab.setVisibility(View.GONE);
        }

        cargarTareas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarTareas();
    }

    private void cargarTareas() {
        listaTareas = tareaDAO.obtenerTodas();

        if (listaTareas.isEmpty()) {
            recyclerTareas.setVisibility(View.GONE);
            layoutVacio.setVisibility(View.VISIBLE);
        } else {
            recyclerTareas.setVisibility(View.VISIBLE);
            layoutVacio.setVisibility(View.GONE);
            boolean edit = RolHelper.puedeCrearTareas(sessionManager.getRol());
            boolean del  = RolHelper.puedeEliminarTareas(sessionManager.getRol());
            recyclerTareas.setAdapter(new TareaAdapter(listaTareas, this, edit, del));
        }
    }

    private void abrirFormulario(Tarea tarea) {
        Intent intent = new Intent(this, FormTareaActivity.class);
        if (tarea != null) {
            intent.putExtra(FormTareaActivity.EXTRA_TAREA_ID, tarea.getId());
        }
        startActivityForResult(intent, REQUEST_FORM);
    }

    @Override
    public void onEditar(Tarea tarea) {
        abrirFormulario(tarea);
    }

    @Override
    public void onEliminar(Tarea tarea) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar tarea")
                .setMessage("¿Estás seguro de que querés eliminar \"" + tarea.getTitulo() + "\"?\n"
                        + "Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    tareaDAO.eliminar(tarea.getId());
                    usuarioDAO.registrarAcceso(
                            sessionManager.getUsuarioId(),
                            "ELIMINAR_TAREA",
                            "Tarea eliminada: " + tarea.getTitulo()
                    );
                    cargarTareas();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FORM && resultCode == RESULT_OK) {
            cargarTareas();
        }
    }
}
