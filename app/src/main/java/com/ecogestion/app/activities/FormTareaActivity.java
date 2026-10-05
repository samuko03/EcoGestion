package com.ecogestion.app.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ecogestion.app.R;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.PlantacionDAO;
import com.ecogestion.app.database.dao.TareaDAO;
import com.ecogestion.app.database.dao.UsuarioDAO;
import com.ecogestion.app.database.dao.ZonaDAO;
import com.ecogestion.app.models.Plantacion;
import com.ecogestion.app.models.Tarea;
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

public class FormTareaActivity extends AppCompatActivity {

    public static final String EXTRA_TAREA_ID = "tarea_id";

    private TextInputLayout   tilTitulo;
    private TextInputEditText etTitulo, etDescripcion, etFechaInicio, etFechaLimite, etObservaciones;
    private Spinner spinnerTipo, spinnerPrioridad, spinnerEstado,
                    spinnerZona, spinnerPlantacion, spinnerAsignado;

    private TareaDAO      tareaDAO;
    private ZonaDAO       zonaDAO;
    private PlantacionDAO plantacionDAO;
    private UsuarioDAO    usuarioDAO;
    private SessionManager sessionManager;

    private Tarea tareaEditando;

    private List<Zona>       listaZonas;
    private List<Plantacion> listaPlantaciones;
    private List<Usuario>    listaUsuarios;

    private static final String[] TIPOS = {
            "PLANTACION", "RIEGO", "PODA", "INSPECCION", "MANTENIMIENTO", "OTRO"
    };
    private static final String[] PRIORIDADES = {"ALTA", "MEDIA", "BAJA"};
    private static final String[] ESTADOS     = {"PENDIENTE", "EN_CURSO", "COMPLETADA", "CANCELADA"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_tarea);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        tareaDAO      = new TareaDAO(db);
        zonaDAO       = new ZonaDAO(db);
        plantacionDAO = new PlantacionDAO(db);
        usuarioDAO    = new UsuarioDAO(db);
        sessionManager= new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Vistas
        tilTitulo       = findViewById(R.id.tilTitulo);
        etTitulo        = findViewById(R.id.etTitulo);
        etDescripcion   = findViewById(R.id.etDescripcion);
        etFechaInicio   = findViewById(R.id.etFechaInicio);
        etFechaLimite   = findViewById(R.id.etFechaLimite);
        etObservaciones = findViewById(R.id.etObservaciones);
        spinnerTipo      = findViewById(R.id.spinnerTipo);
        spinnerPrioridad = findViewById(R.id.spinnerPrioridad);
        spinnerEstado    = findViewById(R.id.spinnerEstado);
        spinnerZona      = findViewById(R.id.spinnerZona);
        spinnerPlantacion= findViewById(R.id.spinnerPlantacion);
        spinnerAsignado  = findViewById(R.id.spinnerAsignado);

        // Date pickers
        etFechaInicio.setOnClickListener(v -> mostrarDatePicker(etFechaInicio));
        etFechaLimite.setOnClickListener(v -> mostrarDatePicker(etFechaLimite));

        // Cargar spinners fijos
        spinnerTipo.setAdapter(simpleAdapter(TIPOS));
        spinnerPrioridad.setAdapter(simpleAdapter(PRIORIDADES));
        spinnerEstado.setAdapter(simpleAdapter(ESTADOS));

        // Cargar spinners dinámicos
        cargarSpinnersDinamicos();

        // ¿Editar o nuevo?
        int tareaId = getIntent().getIntExtra(EXTRA_TAREA_ID, -1);
        if (tareaId != -1) {
            tareaEditando = tareaDAO.obtenerPorId(tareaId);
            cargarDatos();
            toolbar.setTitle("Editar Tarea");
        } else {
            // Prioridad MEDIA por defecto
            spinnerPrioridad.setSelection(1);
            toolbar.setTitle("Nueva Tarea");
        }

        MaterialButton btnGuardar = findViewById(R.id.btnGuardarTarea);
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private ArrayAdapter<String> simpleAdapter(String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void cargarSpinnersDinamicos() {
        // Zonas
        listaZonas = zonaDAO.obtenerTodas();
        List<String> nombresZonas = new ArrayList<>();
        nombresZonas.add("Sin zona");
        for (Zona z : listaZonas) nombresZonas.add(z.getNombre());
        spinnerZona.setAdapter(simpleAdapter(nombresZonas.toArray(new String[0])));

        // Plantaciones
        listaPlantaciones = plantacionDAO.obtenerTodas();
        List<String> nombresPlantaciones = new ArrayList<>();
        nombresPlantaciones.add("Sin plantación");
        for (Plantacion p : listaPlantaciones) nombresPlantaciones.add(p.getNombre());
        spinnerPlantacion.setAdapter(simpleAdapter(nombresPlantaciones.toArray(new String[0])));

        // Usuarios
        listaUsuarios = usuarioDAO.obtenerTodos();
        List<String> nombresUsuarios = new ArrayList<>();
        nombresUsuarios.add("Sin asignar");
        for (Usuario u : listaUsuarios) nombresUsuarios.add(u.getNombre() + " " + u.getApellido());
        spinnerAsignado.setAdapter(simpleAdapter(nombresUsuarios.toArray(new String[0])));
    }

    private void cargarDatos() {
        etTitulo.setText(tareaEditando.getTitulo());
        etDescripcion.setText(tareaEditando.getDescripcion());
        etFechaInicio.setText(tareaEditando.getFechaInicio() != null ? tareaEditando.getFechaInicio() : "");
        etFechaLimite.setText(tareaEditando.getFechaLimite() != null ? tareaEditando.getFechaLimite() : "");
        etObservaciones.setText(tareaEditando.getObservaciones() != null ? tareaEditando.getObservaciones() : "");

        seleccionarEnSpinner(spinnerTipo,      TIPOS,      tareaEditando.getTipo());
        seleccionarEnSpinner(spinnerPrioridad, PRIORIDADES,tareaEditando.getPrioridad());
        seleccionarEnSpinner(spinnerEstado,    ESTADOS,    tareaEditando.getEstado());

        // Zona (índice 0 = Sin zona)
        if (tareaEditando.getZonaId() > 0) {
            for (int i = 0; i < listaZonas.size(); i++) {
                if (listaZonas.get(i).getId() == tareaEditando.getZonaId()) {
                    spinnerZona.setSelection(i + 1);
                    break;
                }
            }
        }

        // Plantación
        if (tareaEditando.getPlantacionId() > 0) {
            for (int i = 0; i < listaPlantaciones.size(); i++) {
                if (listaPlantaciones.get(i).getId() == tareaEditando.getPlantacionId()) {
                    spinnerPlantacion.setSelection(i + 1);
                    break;
                }
            }
        }

        // Asignado
        if (tareaEditando.getAsignadoId() > 0) {
            for (int i = 0; i < listaUsuarios.size(); i++) {
                if (listaUsuarios.get(i).getId() == tareaEditando.getAsignadoId()) {
                    spinnerAsignado.setSelection(i + 1);
                    break;
                }
            }
        }
    }

    private void seleccionarEnSpinner(Spinner spinner, String[] valores, String valor) {
        for (int i = 0; i < valores.length; i++) {
            if (valores[i].equals(valor)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void mostrarDatePicker(TextInputEditText campo) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            campo.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void guardar() {
        if (!validar()) return;

        String titulo       = Objects.requireNonNull(etTitulo.getText()).toString().trim();
        String descripcion  = Objects.requireNonNull(etDescripcion.getText()).toString().trim();
        String tipo         = TIPOS[spinnerTipo.getSelectedItemPosition()];
        String prioridad    = PRIORIDADES[spinnerPrioridad.getSelectedItemPosition()];
        String estado       = ESTADOS[spinnerEstado.getSelectedItemPosition()];
        String fechaInicio  = Objects.requireNonNull(etFechaInicio.getText()).toString().trim();
        String fechaLimite  = Objects.requireNonNull(etFechaLimite.getText()).toString().trim();
        String observaciones= Objects.requireNonNull(etObservaciones.getText()).toString().trim();

        int zonaPos = spinnerZona.getSelectedItemPosition();
        int zonaId  = (zonaPos == 0) ? 0 : listaZonas.get(zonaPos - 1).getId();

        int plPos = spinnerPlantacion.getSelectedItemPosition();
        int plantacionId = (plPos == 0) ? 0 : listaPlantaciones.get(plPos - 1).getId();

        int asigPos = spinnerAsignado.getSelectedItemPosition();
        int asignadoId = (asigPos == 0) ? 0 : listaUsuarios.get(asigPos - 1).getId();

        Tarea t = new Tarea();
        t.setTitulo(titulo);
        t.setDescripcion(descripcion.isEmpty() ? null : descripcion);
        t.setTipo(tipo);
        t.setPrioridad(prioridad);
        t.setEstado(estado);
        t.setZonaId(zonaId);
        t.setPlantacionId(plantacionId);
        t.setAsignadoId(asignadoId);
        t.setFechaInicio(fechaInicio.isEmpty() ? null : fechaInicio);
        t.setFechaLimite(fechaLimite.isEmpty() ? null : fechaLimite);
        t.setObservaciones(observaciones.isEmpty() ? null : observaciones);

        if (tareaEditando == null) {
            long id = tareaDAO.insertar(t);
            if (id > 0) {
                usuarioDAO.registrarAcceso(sessionManager.getUsuarioId(),
                        "CREAR_TAREA", "Tarea creada: " + titulo + " | Prioridad: " + prioridad);
                Toast.makeText(this, "Tarea creada correctamente", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error al crear la tarea", Toast.LENGTH_SHORT).show();
            }
        } else {
            t.setId(tareaEditando.getId());
            int rows = tareaDAO.actualizar(t);
            if (rows > 0) {
                usuarioDAO.registrarAcceso(sessionManager.getUsuarioId(),
                        "MODIFICAR_TAREA", "Tarea modificada: " + titulo);
                Toast.makeText(this, "Tarea actualizada correctamente", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error al actualizar la tarea", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validar() {
        String titulo = Objects.requireNonNull(etTitulo.getText()).toString().trim();
        if (TextUtils.isEmpty(titulo)) {
            tilTitulo.setError("El título es obligatorio");
            return false;
        }
        tilTitulo.setError(null);
        return true;
    }
}
