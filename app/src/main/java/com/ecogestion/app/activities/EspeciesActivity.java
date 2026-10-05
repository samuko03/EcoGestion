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
import com.ecogestion.app.adapters.EspecieAdapter;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.EspecieDAO;
import com.ecogestion.app.database.dao.UsuarioDAO;
import com.ecogestion.app.models.Especie;
import com.ecogestion.app.utils.RolHelper;
import com.ecogestion.app.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class EspeciesActivity extends AppCompatActivity implements EspecieAdapter.OnEspecieListener {

    private RecyclerView recyclerView;
    private LinearLayout layoutVacio;
    private EspecieAdapter adapter;
    private EspecieDAO especieDAO;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_especies);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        session = new SessionManager(this);
        especieDAO = new EspecieDAO(DatabaseHelper.getInstance(this));

        recyclerView = findViewById(R.id.recyclerEspecies);
        layoutVacio = findViewById(R.id.layoutVacio);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        boolean puedeEditar = RolHelper.puedeEditarEspecies(session.getRol());
        if (puedeEditar) {
            findViewById(R.id.fabAgregarEspecie).setOnClickListener(v ->
                    startActivity(new Intent(this, FormEspecieActivity.class)));
        } else {
            findViewById(R.id.fabAgregarEspecie).setVisibility(View.GONE);
        }

        cargarEspecies();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarEspecies();
    }

    private void cargarEspecies() {
        List<Especie> lista = especieDAO.obtenerTodas();
        if (lista.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutVacio.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutVacio.setVisibility(View.GONE);
        }
        boolean puedeEditar = RolHelper.puedeEditarEspecies(session.getRol());
        if (adapter == null) {
            adapter = new EspecieAdapter(this, lista, this, puedeEditar);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.actualizarLista(lista);
        }
    }

    @Override
    public void onEditar(Especie especie) {
        Intent intent = new Intent(this, FormEspecieActivity.class);
        intent.putExtra("especie_id", especie.getId());
        startActivity(intent);
    }

    @Override
    public void onEliminar(Especie especie) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar especie")
                .setMessage("¿Estás seguro de que querés eliminar \"" + especie.getNombre() + "\"?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    especieDAO.eliminar(especie.getId());
                    new UsuarioDAO(DatabaseHelper.getInstance(this))
                            .registrarAcceso(session.getUsuarioId(), "ELIMINAR_ESPECIE",
                                    "Especie eliminada: " + especie.getNombre());
                    Toast.makeText(this, "Especie eliminada", Toast.LENGTH_SHORT).show();
                    cargarEspecies();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
