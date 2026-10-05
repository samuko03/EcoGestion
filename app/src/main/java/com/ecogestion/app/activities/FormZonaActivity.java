package com.ecogestion.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ecogestion.app.R;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.UsuarioDAO;
import com.ecogestion.app.database.dao.ZonaDAO;
import com.ecogestion.app.models.Zona;
import com.ecogestion.app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class FormZonaActivity extends AppCompatActivity {

    private TextInputLayout tilNombre, tilDepartamento, tilLocalidad;
    private TextInputEditText etNombre, etDepartamento, etLocalidad, etLatitud, etLongitud;
    private Spinner spinnerEstado;

    private ZonaDAO zonaDAO;
    private SessionManager session;
    private Zona zonaEditar;
    private boolean esEdicion = false;

    private final String[] ESTADOS = {"PENDIENTE", "ACTIVA", "FINALIZADA"};

    // Launcher para recibir coordenadas del mapa
    private final ActivityResultLauncher<Intent> mapaLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            double lat = result.getData().getDoubleExtra(
                                    SeleccionarUbicacionActivity.EXTRA_LATITUD, 0);
                            double lng = result.getData().getDoubleExtra(
                                    SeleccionarUbicacionActivity.EXTRA_LONGITUD, 0);
                            etLatitud.setText(String.format("%.6f", lat));
                            etLongitud.setText(String.format("%.6f", lng));
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_zona);

        session = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        zonaDAO = new ZonaDAO(dbHelper);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        inicializarVistas();
        configurarSpinner();

        int zonaId = getIntent().getIntExtra("zona_id", -1);
        if (zonaId != -1) {
            esEdicion = true;
            zonaEditar = zonaDAO.obtenerPorId(zonaId);
            if (zonaEditar != null) cargarDatos(zonaEditar);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle("Editar Zona");
        }

        findViewById(R.id.btnGuardarZona).setOnClickListener(v -> guardar());

        // Botón abrir mapa — pasa coords existentes si las hay
        findViewById(R.id.btnSeleccionarUbicacion).setOnClickListener(v -> {
            Intent intent = new Intent(this, SeleccionarUbicacionActivity.class);
            String latStr = etLatitud.getText() != null ? etLatitud.getText().toString().trim() : "";
            String lngStr = etLongitud.getText() != null ? etLongitud.getText().toString().trim() : "";
            try {
                if (!latStr.isEmpty() && !lngStr.isEmpty()) {
                    intent.putExtra(SeleccionarUbicacionActivity.EXTRA_LATITUD, Double.parseDouble(latStr));
                    intent.putExtra(SeleccionarUbicacionActivity.EXTRA_LONGITUD, Double.parseDouble(lngStr));
                }
            } catch (NumberFormatException ignored) {}
            mapaLauncher.launch(intent);
        });
    }

    private void inicializarVistas() {
        tilNombre = findViewById(R.id.tilNombre);
        tilDepartamento = findViewById(R.id.tilDepartamento);
        tilLocalidad = findViewById(R.id.tilLocalidad);
        etNombre = findViewById(R.id.etNombre);
        etDepartamento = findViewById(R.id.etDepartamento);
        etLocalidad = findViewById(R.id.etLocalidad);
        etLatitud = findViewById(R.id.etLatitud);
        etLongitud = findViewById(R.id.etLongitud);
        spinnerEstado = findViewById(R.id.spinnerEstado);
    }

    private void configurarSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ESTADOS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);
    }

    private void cargarDatos(Zona zona) {
        etNombre.setText(zona.getNombre());
        etDepartamento.setText(zona.getDepartamento());
        etLocalidad.setText(zona.getLocalidad());
        etLatitud.setText(zona.getLatitud());
        etLongitud.setText(zona.getLongitud());
        for (int i = 0; i < ESTADOS.length; i++) {
            if (ESTADOS[i].equals(zona.getEstado())) {
                spinnerEstado.setSelection(i);
                break;
            }
        }
    }

    private void guardar() {
        tilNombre.setError(null);
        tilDepartamento.setError(null);
        tilLocalidad.setError(null);

        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String departamento = etDepartamento.getText() != null ? etDepartamento.getText().toString().trim() : "";
        String localidad = etLocalidad.getText() != null ? etLocalidad.getText().toString().trim() : "";
        String latitud = etLatitud.getText() != null ? etLatitud.getText().toString().trim() : "";
        String longitud = etLongitud.getText() != null ? etLongitud.getText().toString().trim() : "";
        String estado = ESTADOS[spinnerEstado.getSelectedItemPosition()];

        if (TextUtils.isEmpty(nombre)) { tilNombre.setError("Campo obligatorio"); return; }
        if (TextUtils.isEmpty(departamento)) { tilDepartamento.setError("Campo obligatorio"); return; }
        if (TextUtils.isEmpty(localidad)) { tilLocalidad.setError("Campo obligatorio"); return; }

        Zona zona = esEdicion ? zonaEditar : new Zona();
        zona.setNombre(nombre);
        zona.setDepartamento(departamento);
        zona.setLocalidad(localidad);
        zona.setLatitud(latitud);
        zona.setLongitud(longitud);
        zona.setEstado(estado);
        zona.setResponsableId(session.getUsuarioId());

        UsuarioDAO usuarioDAO = new UsuarioDAO(DatabaseHelper.getInstance(this));

        if (esEdicion) {
            zonaDAO.actualizar(zona);
            usuarioDAO.registrarAcceso(session.getUsuarioId(), "MODIFICAR_ZONA", "Zona editada: " + nombre);
            Toast.makeText(this, "Zona actualizada", Toast.LENGTH_SHORT).show();
        } else {
            zonaDAO.insertar(zona);
            usuarioDAO.registrarAcceso(session.getUsuarioId(), "CREAR_ZONA", "Zona creada: " + nombre);
            Toast.makeText(this, "Zona registrada", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
