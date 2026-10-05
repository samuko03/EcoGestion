package com.ecogestion.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ecogestion.app.R;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.EspecieDAO;
import com.ecogestion.app.database.dao.UsuarioDAO;
import com.ecogestion.app.models.Especie;
import com.ecogestion.app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class FormEspecieActivity extends AppCompatActivity {

    private TextInputLayout tilNombre, tilEcorregion, tilCantidad;
    private TextInputEditText etNombre, etNombreCientifico, etEcorregion,
            etDescripcion, etCantidad;

    private EspecieDAO especieDAO;
    private SessionManager session;
    private Especie especieEditar;
    private boolean esEdicion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_especie);

        session = new SessionManager(this);
        especieDAO = new EspecieDAO(DatabaseHelper.getInstance(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        inicializarVistas();

        int especieId = getIntent().getIntExtra("especie_id", -1);
        if (especieId != -1) {
            esEdicion = true;
            especieEditar = especieDAO.obtenerPorId(especieId);
            if (especieEditar != null) cargarDatos(especieEditar);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle("Editar Especie");
        }

        findViewById(R.id.btnGuardarEspecie).setOnClickListener(v -> guardar());
    }

    private void inicializarVistas() {
        tilNombre = findViewById(R.id.tilNombre);
        tilEcorregion = findViewById(R.id.tilEcorregion);
        tilCantidad = findViewById(R.id.tilCantidad);
        etNombre = findViewById(R.id.etNombre);
        etNombreCientifico = findViewById(R.id.etNombreCientifico);
        etEcorregion = findViewById(R.id.etEcorregion);
        etDescripcion = findViewById(R.id.etDescripcion);
        etCantidad = findViewById(R.id.etCantidad);
    }

    private void cargarDatos(Especie e) {
        etNombre.setText(e.getNombre());
        etNombreCientifico.setText(e.getNombreCientifico());
        etEcorregion.setText(e.getEcorregion());
        etDescripcion.setText(e.getDescripcion());
        etCantidad.setText(String.valueOf(e.getCantidadDisponible()));
    }

    private void guardar() {
        tilNombre.setError(null);
        tilEcorregion.setError(null);
        tilCantidad.setError(null);

        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String cientifico = etNombreCientifico.getText() != null ? etNombreCientifico.getText().toString().trim() : "";
        String ecorregion = etEcorregion.getText() != null ? etEcorregion.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        String cantStr = etCantidad.getText() != null ? etCantidad.getText().toString().trim() : "";

        if (TextUtils.isEmpty(nombre)) { tilNombre.setError("Campo obligatorio"); return; }
        if (TextUtils.isEmpty(ecorregion)) { tilEcorregion.setError("Campo obligatorio"); return; }
        if (TextUtils.isEmpty(cantStr)) { tilCantidad.setError("Campo obligatorio"); return; }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantStr);
        } catch (NumberFormatException ex) {
            tilCantidad.setError("Ingresá un número válido");
            return;
        }

        Especie especie = esEdicion ? especieEditar : new Especie();
        especie.setNombre(nombre);
        especie.setNombreCientifico(cientifico);
        especie.setEcorregion(ecorregion);
        especie.setDescripcion(descripcion);
        especie.setCantidadDisponible(cantidad);

        UsuarioDAO usuarioDAO = new UsuarioDAO(DatabaseHelper.getInstance(this));

        if (esEdicion) {
            especieDAO.actualizar(especie);
            usuarioDAO.registrarAcceso(session.getUsuarioId(), "MODIFICAR_ESPECIE",
                    "Especie editada: " + nombre);
            Toast.makeText(this, "Especie actualizada", Toast.LENGTH_SHORT).show();
        } else {
            especieDAO.insertar(especie);
            usuarioDAO.registrarAcceso(session.getUsuarioId(), "CREAR_ESPECIE",
                    "Especie creada: " + nombre);
            Toast.makeText(this, "Especie registrada", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
