package com.ecogestion.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ecogestion.app.R;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.EspecieDAO;
import com.ecogestion.app.database.dao.PlantacionDAO;
import com.ecogestion.app.database.dao.UsuarioDAO;
import com.ecogestion.app.database.dao.ZonaDAO;
import com.ecogestion.app.models.Especie;
import com.ecogestion.app.models.Plantacion;
import com.ecogestion.app.models.Usuario;
import com.ecogestion.app.models.Zona;
import com.ecogestion.app.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class FormPlantacionActivity extends AppCompatActivity {

    public static final String EXTRA_PLANTACION_ID = "plantacion_id";

    private TextInputLayout    tilNombre, tilCantidad;
    private TextInputEditText  etNombre, etCantidad, etFecha;
    private Spinner            spinnerZona, spinnerEspecie, spinnerEstado, spinnerResponsable;
    private TextView           tvUbicacion;

    private double latitudSeleccionada  = 0;
    private double longitudSeleccionada = 0;

    // Launcher para recibir resultado del selector de mapa
    private final ActivityResultLauncher<Intent> mapaLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            latitudSeleccionada  = result.getData()
                                    .getDoubleExtra(SeleccionarUbicacionActivity.EXTRA_LATITUD, 0);
                            longitudSeleccionada = result.getData()
                                    .getDoubleExtra(SeleccionarUbicacionActivity.EXTRA_LONGITUD, 0);
                            mostrarCoordenadas();
                        }
                    });

    private PlantacionDAO plantacionDAO;
    private ZonaDAO       zonaDAO;
    private EspecieDAO    especieDAO;
    private UsuarioDAO    usuarioDAO;
    private SessionManager sessionManager;

    private Plantacion plantacionEditando;

    private List<Zona>    listaZonas;
    private List<Especie> listaEspecies;
    private List<Usuario> listaUsuarios;

    private static final String[] ESTADOS = {
            "PLANIFICADA", "EN_PROCESO", "COMPLETADA", "CANCELADA"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_plantacion);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        plantacionDAO  = new PlantacionDAO(db);
        zonaDAO        = new ZonaDAO(db);
        especieDAO     = new EspecieDAO(db);
        usuarioDAO     = new UsuarioDAO(db);
        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Vistas
        tilNombre    = findViewById(R.id.tilNombre);
        tilCantidad  = findViewById(R.id.tilCantidad);
        etNombre     = findViewById(R.id.etNombre);
        etCantidad   = findViewById(R.id.etCantidad);
        etFecha      = findViewById(R.id.etFecha);
        spinnerZona        = findViewById(R.id.spinnerZona);
        spinnerEspecie     = findViewById(R.id.spinnerEspecie);
        spinnerEstado      = findViewById(R.id.spinnerEstado);
        spinnerResponsable = findViewById(R.id.spinnerResponsable);
        tvUbicacion        = findViewById(R.id.tvUbicacion);

        // Date picker
        etFecha.setOnClickListener(v -> mostrarDatePicker());

        // Botón seleccionar ubicación en mapa
        findViewById(R.id.btnSeleccionarUbicacion).setOnClickListener(v -> abrirMapa());

        // Cargar datos en spinners
        cargarSpinners();

        // Cargar estados
        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, ESTADOS);
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(estadoAdapter);

        // ¿Editar o nuevo?
        int plantacionId = getIntent().getIntExtra(EXTRA_PLANTACION_ID, -1);
        if (plantacionId != -1) {
            plantacionEditando = plantacionDAO.obtenerPorId(plantacionId);
            cargarDatos();
            toolbar.setTitle("Editar Plantación");
        } else {
            toolbar.setTitle("Nueva Plantación");
        }
        mostrarCoordenadas(); // actualiza el TextView de ubicación

        MaterialButton btnGuardar = findViewById(R.id.btnGuardarPlantacion);
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void cargarSpinners() {
        // Zonas
        listaZonas = zonaDAO.obtenerTodas();
        List<String> nombresZonas = new ArrayList<>();
        for (Zona z : listaZonas) nombresZonas.add(z.getNombre());
        ArrayAdapter<String> zonaAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, nombresZonas);
        zonaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerZona.setAdapter(zonaAdapter);

        // Especies
        listaEspecies = especieDAO.obtenerTodas();
        List<String> nombresEspecies = new ArrayList<>();
        for (Especie e : listaEspecies) nombresEspecies.add(e.getNombre());
        ArrayAdapter<String> especieAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, nombresEspecies);
        especieAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEspecie.setAdapter(especieAdapter);

        // Usuarios (responsable opcional)
        listaUsuarios = usuarioDAO.obtenerTodos();
        List<String> nombresUsuarios = new ArrayList<>();
        nombresUsuarios.add("Sin asignar");
        for (Usuario u : listaUsuarios) nombresUsuarios.add(u.getNombre() + " " + u.getApellido());
        ArrayAdapter<String> usuarioAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, nombresUsuarios);
        usuarioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResponsable.setAdapter(usuarioAdapter);
    }

    private void cargarDatos() {
        // Si la plantación ya tenía ubicación, recuperarla
        if (plantacionEditando.tieneUbicacion()) {
            latitudSeleccionada  = plantacionEditando.getLatitud();
            longitudSeleccionada = plantacionEditando.getLongitud();
        }

        etNombre.setText(plantacionEditando.getNombre());
        etCantidad.setText(String.valueOf(plantacionEditando.getCantidadArboles()));
        etFecha.setText(plantacionEditando.getFechaPlantacion() != null
                ? plantacionEditando.getFechaPlantacion() : "");

        // Seleccionar zona
        for (int i = 0; i < listaZonas.size(); i++) {
            if (listaZonas.get(i).getId() == plantacionEditando.getZonaId()) {
                spinnerZona.setSelection(i);
                break;
            }
        }

        // Seleccionar especie
        for (int i = 0; i < listaEspecies.size(); i++) {
            if (listaEspecies.get(i).getId() == plantacionEditando.getEspecieId()) {
                spinnerEspecie.setSelection(i);
                break;
            }
        }

        // Seleccionar estado
        for (int i = 0; i < ESTADOS.length; i++) {
            if (ESTADOS[i].equals(plantacionEditando.getEstado())) {
                spinnerEstado.setSelection(i);
                break;
            }
        }

        // Seleccionar responsable (índice 0 = Sin asignar, 1..n = usuarios)
        if (plantacionEditando.getResponsableId() > 0) {
            for (int i = 0; i < listaUsuarios.size(); i++) {
                if (listaUsuarios.get(i).getId() == plantacionEditando.getResponsableId()) {
                    spinnerResponsable.setSelection(i + 1); // +1 por "Sin asignar"
                    break;
                }
            }
        }
    }

    // ── Mapa ───────────────────────────────────────────────────
    private void abrirMapa() {
        Intent intent = new Intent(this, SeleccionarUbicacionActivity.class);
        // Si ya hay coordenadas, pasarlas para que el mapa las muestre
        if (latitudSeleccionada != 0 || longitudSeleccionada != 0) {
            intent.putExtra(SeleccionarUbicacionActivity.EXTRA_LATITUD,  latitudSeleccionada);
            intent.putExtra(SeleccionarUbicacionActivity.EXTRA_LONGITUD, longitudSeleccionada);
        }
        mapaLauncher.launch(intent);
    }

    private void mostrarCoordenadas() {
        if (tvUbicacion == null) return;
        if (latitudSeleccionada != 0 || longitudSeleccionada != 0) {
            tvUbicacion.setText(String.format("Lat: %.6f   Lng: %.6f",
                    latitudSeleccionada, longitudSeleccionada));
            tvUbicacion.setTextColor(getResources().getColor(R.color.carbon_text, null));
        } else {
            tvUbicacion.setText("Sin ubicación seleccionada");
            tvUbicacion.setTextColor(getResources().getColor(R.color.gray_hint, null));
        }
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fecha = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
            etFecha.setText(fecha);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void guardar() {
        if (!validar()) return;

        String nombre   = Objects.requireNonNull(etNombre.getText()).toString().trim();
        int    cantidad = Integer.parseInt(Objects.requireNonNull(etCantidad.getText()).toString().trim());
        String fecha    = Objects.requireNonNull(etFecha.getText()).toString().trim();
        String estado   = ESTADOS[spinnerEstado.getSelectedItemPosition()];

        // Zona seleccionada
        int zonaPos = spinnerZona.getSelectedItemPosition();
        int zonaId  = listaZonas.isEmpty() ? 0 : listaZonas.get(zonaPos).getId();

        // Especie seleccionada
        int especiePos = spinnerEspecie.getSelectedItemPosition();
        int especieId  = listaEspecies.isEmpty() ? 0 : listaEspecies.get(especiePos).getId();

        // Responsable (0 = Sin asignar)
        int respPos = spinnerResponsable.getSelectedItemPosition();
        int responsableId = (respPos == 0) ? 0 : listaUsuarios.get(respPos - 1).getId();

        Plantacion p = new Plantacion();
        p.setNombre(nombre);
        p.setZonaId(zonaId);
        p.setEspecieId(especieId);
        p.setCantidadArboles(cantidad);
        p.setFechaPlantacion(fecha.isEmpty() ? null : fecha);
        p.setEstado(estado);
        p.setResponsableId(responsableId);
        p.setObservaciones(
                Objects.requireNonNull(
                        ((TextInputEditText) findViewById(R.id.etObservaciones)).getText()
                ).toString().trim());
        p.setLatitud(latitudSeleccionada);
        p.setLongitud(longitudSeleccionada);

        if (plantacionEditando == null) {
            // CREAR
            long id = plantacionDAO.insertar(p);
            if (id > 0) {
                usuarioDAO.registrarAcceso(sessionManager.getUsuarioId(),
                        "CREAR_PLANTACION", "Plantación creada: " + nombre);
                Toast.makeText(this, "Plantación creada correctamente", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error al crear la plantación", Toast.LENGTH_SHORT).show();
            }
        } else {
            // ACTUALIZAR
            p.setId(plantacionEditando.getId());
            int rows = plantacionDAO.actualizar(p);
            if (rows > 0) {
                usuarioDAO.registrarAcceso(sessionManager.getUsuarioId(),
                        "MODIFICAR_PLANTACION", "Plantación modificada: " + nombre);
                Toast.makeText(this, "Plantación actualizada correctamente", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error al actualizar la plantación", Toast.LENGTH_SHORT).show();
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

        String cantidadStr = Objects.requireNonNull(etCantidad.getText()).toString().trim();
        if (TextUtils.isEmpty(cantidadStr)) {
            tilCantidad.setError("La cantidad es obligatoria");
            ok = false;
        } else {
            try {
                int cant = Integer.parseInt(cantidadStr);
                if (cant <= 0) {
                    tilCantidad.setError("Debe ser mayor a 0");
                    ok = false;
                } else {
                    tilCantidad.setError(null);
                }
            } catch (NumberFormatException e) {
                tilCantidad.setError("Número inválido");
                ok = false;
            }
        }

        if (listaZonas.isEmpty()) {
            Toast.makeText(this, "No hay zonas disponibles. Creá una zona primero.", Toast.LENGTH_LONG).show();
            ok = false;
        }

        if (listaEspecies.isEmpty()) {
            Toast.makeText(this, "No hay especies disponibles. Agregá una especie primero.", Toast.LENGTH_LONG).show();
            ok = false;
        }

        return ok;
    }
}
