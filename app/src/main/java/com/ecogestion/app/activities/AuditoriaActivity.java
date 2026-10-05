package com.ecogestion.app.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecogestion.app.R;
import com.ecogestion.app.adapters.AuditoriaAdapter;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.AuditoriaDAO;
import com.ecogestion.app.models.Auditoria;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class AuditoriaActivity extends AppCompatActivity {

    private RecyclerView      recycler;
    private View              layoutVacio;
    private TextView          tvTotal;
    private AuditoriaDAO      dao;
    private AuditoriaAdapter  adapter;

    private String filtroActivo  = "TODOS";
    private String busquedaActual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auditoria);

        dao = new AuditoriaDAO(DatabaseHelper.getInstance(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        recycler    = findViewById(R.id.recyclerAuditoria);
        layoutVacio = findViewById(R.id.layoutVacio);
        tvTotal     = findViewById(R.id.tvTotalRegistros);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Botones de filtro
        findViewById(R.id.btnFiltroTodos)     .setOnClickListener(v -> aplicarFiltro("TODOS"));
        findViewById(R.id.btnFiltroCrear)     .setOnClickListener(v -> aplicarFiltro("CREAR"));
        findViewById(R.id.btnFiltroModificar) .setOnClickListener(v -> aplicarFiltro("MODIFICAR"));
        findViewById(R.id.btnFiltroEliminar)  .setOnClickListener(v -> aplicarFiltro("ELIMINAR"));
        findViewById(R.id.btnFiltroSistema)   .setOnClickListener(v -> aplicarFiltro("SISTEMA"));

        // Búsqueda en tiempo real
        TextInputEditText etBusqueda = findViewById(R.id.etBusqueda);
        etBusqueda.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                busquedaActual = s != null ? s.toString() : "";
                cargarDatos();
            }
        });

        cargarDatos();
    }

    private void aplicarFiltro(String categoria) {
        filtroActivo = categoria;
        resaltarBotonActivo(categoria);
        cargarDatos();
    }

    private void cargarDatos() {
        List<Auditoria> lista = dao.obtenerFiltrados(filtroActivo, busquedaActual);

        // Contador
        int total = dao.contarTodos();
        boolean hayFiltro = !filtroActivo.equals("TODOS") || !busquedaActual.isEmpty();
        tvTotal.setText(lista.size() + " registro" + (lista.size() != 1 ? "s" : "")
                + (hayFiltro ? " filtrados" : "")
                + "  ·  Total histórico: " + total);

        if (lista.isEmpty()) {
            recycler.setVisibility(View.GONE);
            layoutVacio.setVisibility(View.VISIBLE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            layoutVacio.setVisibility(View.GONE);
            if (adapter == null) {
                adapter = new AuditoriaAdapter(lista);
                recycler.setAdapter(adapter);
            } else {
                adapter.actualizar(lista);
            }
        }
    }

    private void resaltarBotonActivo(String categoria) {
        // Resetear todos a outlined
        int[] ids = {
            R.id.btnFiltroTodos, R.id.btnFiltroCrear,
            R.id.btnFiltroModificar, R.id.btnFiltroEliminar, R.id.btnFiltroSistema
        };
        String[] cats = { "TODOS", "CREAR", "MODIFICAR", "ELIMINAR", "SISTEMA" };

        for (int i = 0; i < ids.length; i++) {
            MaterialButton btn = findViewById(ids[i]);
            if (cats[i].equals(categoria)) {
                btn.setBackgroundColor(getResources().getColor(R.color.inst_blue, null));
                btn.setTextColor(getResources().getColor(R.color.white, null));
            } else {
                btn.setBackgroundColor(getResources().getColor(R.color.white, null));
                // Color de texto según categoría
                switch (cats[i]) {
                    case "CREAR":
                        btn.setTextColor(getResources().getColor(R.color.green_primary, null)); break;
                    case "ELIMINAR":
                        btn.setTextColor(0xFFE53935); break;
                    case "MODIFICAR":
                        btn.setTextColor(getResources().getColor(R.color.inst_blue, null)); break;
                    default:
                        btn.setTextColor(getResources().getColor(R.color.gray_hint, null)); break;
                }
            }
        }
    }
}
