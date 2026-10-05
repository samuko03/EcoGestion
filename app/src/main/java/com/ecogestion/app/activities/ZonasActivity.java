package com.ecogestion.app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecogestion.app.R;
import com.ecogestion.app.adapters.ZonaAdapter;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.ZonaDAO;
import com.ecogestion.app.database.dao.UsuarioDAO;
import com.ecogestion.app.models.Zona;
import com.ecogestion.app.utils.RolHelper;
import com.ecogestion.app.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ZonasActivity extends AppCompatActivity implements ZonaAdapter.OnZonaListener {

    public static final int REQUEST_FORM = 100;

    private RecyclerView recyclerView;
    private LinearLayout layoutVacio;
    private ZonaAdapter adapter;
    private ZonaDAO zonaDAO;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zonas);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        session = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        zonaDAO = new ZonaDAO(dbHelper);

        recyclerView = findViewById(R.id.recyclerZonas);
        layoutVacio = findViewById(R.id.layoutVacio);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAgregarZona);
        boolean puedeEditar = RolHelper.puedeEditarZonas(session.getRol());
        if (puedeEditar) {
            fab.setOnClickListener(v -> abrirFormulario(null));
        } else {
            fab.setVisibility(View.GONE);
        }

        cargarZonas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarZonas();
    }

    private void cargarZonas() {
        List<Zona> lista = zonaDAO.obtenerTodas();

        if (lista.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutVacio.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutVacio.setVisibility(View.GONE);
        }

        boolean puedeEditar = RolHelper.puedeEditarZonas(session.getRol());
        if (adapter == null) {
            adapter = new ZonaAdapter(this, lista, this, puedeEditar);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.actualizarLista(lista);
        }
    }

    private void abrirFormulario(Zona zona) {
        Intent intent = new Intent(this, FormZonaActivity.class);
        if (zona != null) intent.putExtra("zona_id", zona.getId());
        startActivityForResult(intent, REQUEST_FORM);
    }

    @Override
    public void onEditar(Zona zona) {
        abrirFormulario(zona);
    }

    @Override
    public void onEliminar(Zona zona) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar zona")
                .setMessage("¿Estás seguro de que querés eliminar \"" + zona.getNombre() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    zonaDAO.eliminar(zona.getId());
                    registrarAuditoria("ELIMINAR_ZONA", "Zona eliminada: " + zona.getNombre());
                    Toast.makeText(this, "Zona eliminada", Toast.LENGTH_SHORT).show();
                    cargarZonas();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void registrarAuditoria(String accion, String detalle) {
        UsuarioDAO usuarioDAO = new UsuarioDAO(DatabaseHelper.getInstance(this));
        usuarioDAO.registrarAcceso(session.getUsuarioId(), accion, detalle);
    }
}
