package com.ecogestion.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecogestion.app.R;
import com.ecogestion.app.adapters.PlantacionAdapter;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.EspecieDAO;
import com.ecogestion.app.database.dao.PlantacionDAO;
import com.ecogestion.app.database.dao.UsuarioDAO;
import com.ecogestion.app.database.dao.ZonaDAO;
import com.ecogestion.app.models.Especie;
import com.ecogestion.app.models.Plantacion;
import com.ecogestion.app.models.Zona;
import com.ecogestion.app.utils.ExcelGenerator;
import com.ecogestion.app.utils.ReporteGenerator;
import com.ecogestion.app.utils.RolHelper;
import com.ecogestion.app.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlantacionesActivity extends AppCompatActivity
        implements PlantacionAdapter.OnPlantacionListener {

    private static final int REQUEST_FORM = 1;
    private static final SimpleDateFormat FMT = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "AR"));

    // Views
    private RecyclerView recyclerPlantaciones;
    private View          layoutVacio;
    private TextView      tvVacioSub;
    private TextView      tvResultados;
    private Spinner       spinnerZona;
    private Spinner       spinnerEspecie;
    private MaterialButton btnFechaDesde;
    private MaterialButton btnFechaHasta;

    // DAO / Utils
    private PlantacionDAO    plantacionDAO;
    private UsuarioDAO       usuarioDAO;
    private SessionManager   sessionManager;
    private ReporteGenerator reporteGenerator;
    private ExcelGenerator   excelGenerator;

    // Datos spinners
    private List<Zona>    listaZonas;
    private List<Especie> listaEspecies;

    // Filtros activos
    private String fechaDesde = "";
    private String fechaHasta = "";

    // Lista actual
    private List<Plantacion> listaActual = new ArrayList<>();
    private PlantacionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plantaciones);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        plantacionDAO    = new PlantacionDAO(db);
        usuarioDAO       = new UsuarioDAO(db);
        sessionManager   = new SessionManager(this);
        reporteGenerator = new ReporteGenerator(this);
        excelGenerator   = new ExcelGenerator(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Referencias
        recyclerPlantaciones = findViewById(R.id.recyclerPlantaciones);
        layoutVacio          = findViewById(R.id.layoutVacio);
        tvVacioSub           = findViewById(R.id.tvVacioSub);
        tvResultados         = findViewById(R.id.tvResultados);
        spinnerZona          = findViewById(R.id.spinnerZona);
        spinnerEspecie       = findViewById(R.id.spinnerEspecie);
        btnFechaDesde        = findViewById(R.id.btnFechaDesde);
        btnFechaHasta        = findViewById(R.id.btnFechaHasta);

        recyclerPlantaciones.setLayoutManager(new LinearLayoutManager(this));

        // Cargar spinners
        cargarSpinners(db);

        // Listeners de botones
        btnFechaDesde.setOnClickListener(v -> mostrarDatePicker(true));
        btnFechaHasta.setOnClickListener(v -> mostrarDatePicker(false));
        findViewById(R.id.btnFiltrar).setOnClickListener(v -> aplicarFiltros());
        findViewById(R.id.btnLimpiarFiltros).setOnClickListener(v -> limpiarFiltros());

        // Exportar: solo quien tenga permiso
        String rol = sessionManager.getRol();
        if (RolHelper.puedeExportarReportes(rol)) {
            findViewById(R.id.btnExportarPdf).setOnClickListener(v -> exportarPdf());
            findViewById(R.id.btnExportarExcel).setOnClickListener(v -> exportarExcel());
        } else {
            findViewById(R.id.btnExportarPdf).setVisibility(View.GONE);
            findViewById(R.id.btnExportarExcel).setVisibility(View.GONE);
        }

        // FAB agregar: solo quien pueda crear plantaciones
        if (RolHelper.puedeCrearPlantaciones(rol)) {
            findViewById(R.id.fabAgregarPlantacion).setOnClickListener(v -> abrirFormulario(null));
        } else {
            findViewById(R.id.fabAgregarPlantacion).setVisibility(View.GONE);
        }

        cargarPlantaciones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPlantaciones();
    }

    // ── Spinners ───────────────────────────────────────────────

    private void cargarSpinners(DatabaseHelper db) {
        ZonaDAO    zonaDAO    = new ZonaDAO(db);
        EspecieDAO especieDAO = new EspecieDAO(db);

        listaZonas    = zonaDAO.obtenerTodas();
        listaEspecies = especieDAO.obtenerTodas();

        // Spinner zona
        List<String> nombresZona = new ArrayList<>();
        nombresZona.add("Todas las zonas");
        for (Zona z : listaZonas) nombresZona.add(z.getNombre());
        ArrayAdapter<String> adZona = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombresZona);
        adZona.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerZona.setAdapter(adZona);

        // Spinner especie
        List<String> nombresEsp = new ArrayList<>();
        nombresEsp.add("Todas las especies");
        for (Especie e : listaEspecies) nombresEsp.add(e.getNombre());
        ArrayAdapter<String> adEsp = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombresEsp);
        adEsp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEspecie.setAdapter(adEsp);
    }

    // ── DatePicker ─────────────────────────────────────────────

    private void mostrarDatePicker(boolean esDesde) {
        Calendar cal = Calendar.getInstance();
        // Pre-cargar fecha actual si ya tiene valor
        String fechaActual = esDesde ? fechaDesde : fechaHasta;
        if (fechaActual != null && !fechaActual.isEmpty()) {
            try {
                cal.setTime(FMT.parse(fechaActual));
            } catch (Exception ignored) { }
        }

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fecha = String.format(new Locale("es", "AR"), "%02d/%02d/%04d",
                    dayOfMonth, month + 1, year);
            if (esDesde) {
                fechaDesde = fecha;
                btnFechaDesde.setText("Desde: " + fecha);
            } else {
                fechaHasta = fecha;
                btnFechaHasta.setText("Hasta: " + fecha);
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // ── Filtros ────────────────────────────────────────────────

    private int zonaIdSeleccionada() {
        int pos = spinnerZona.getSelectedItemPosition();
        if (pos <= 0 || pos > listaZonas.size()) return 0;
        return listaZonas.get(pos - 1).getId();
    }

    private int especieIdSeleccionada() {
        int pos = spinnerEspecie.getSelectedItemPosition();
        if (pos <= 0 || pos > listaEspecies.size()) return 0;
        return listaEspecies.get(pos - 1).getId();
    }

    private void aplicarFiltros() {
        cargarPlantaciones();
    }

    private void limpiarFiltros() {
        spinnerZona.setSelection(0);
        spinnerEspecie.setSelection(0);
        fechaDesde = "";
        fechaHasta = "";
        btnFechaDesde.setText("Desde");
        btnFechaHasta.setText("Hasta");
        cargarPlantaciones();
    }

    private void cargarPlantaciones() {
        int zonaId    = zonaIdSeleccionada();
        int especieId = especieIdSeleccionada();

        listaActual = plantacionDAO.obtenerFiltradas(zonaId, especieId, fechaDesde, fechaHasta);

        boolean hayFiltro = zonaId > 0 || especieId > 0
                || !fechaDesde.isEmpty() || !fechaHasta.isEmpty();

        if (listaActual.isEmpty()) {
            recyclerPlantaciones.setVisibility(View.GONE);
            tvResultados.setVisibility(View.GONE);
            layoutVacio.setVisibility(View.VISIBLE);
            tvVacioSub.setText(hayFiltro
                    ? "Ninguna plantación coincide con los filtros aplicados."
                    : "");
        } else {
            recyclerPlantaciones.setVisibility(View.VISIBLE);
            layoutVacio.setVisibility(View.GONE);
            tvResultados.setVisibility(View.VISIBLE);
            tvResultados.setText(listaActual.size()
                    + (listaActual.size() == 1 ? " plantación" : " plantaciones")
                    + (hayFiltro ? " (filtrado)" : ""));
            boolean edit = RolHelper.puedeCrearPlantaciones(sessionManager.getRol());
            boolean del  = RolHelper.puedeEliminarPlantaciones(sessionManager.getRol());
            adapter = new PlantacionAdapter(listaActual, this, edit, del);
            recyclerPlantaciones.setAdapter(adapter);
        }
    }

    // ── Exportar ───────────────────────────────────────────────

    private String construirSubtitulo() {
        StringBuilder sb = new StringBuilder();
        int zonaId    = zonaIdSeleccionada();
        int especieId = especieIdSeleccionada();
        if (zonaId > 0 && spinnerZona.getSelectedItemPosition() > 0) {
            sb.append("Zona: ").append(spinnerZona.getSelectedItem()).append("  ");
        }
        if (especieId > 0 && spinnerEspecie.getSelectedItemPosition() > 0) {
            sb.append("Especie: ").append(spinnerEspecie.getSelectedItem()).append("  ");
        }
        if (!fechaDesde.isEmpty()) sb.append("Desde: ").append(fechaDesde).append("  ");
        if (!fechaHasta.isEmpty()) sb.append("Hasta: ").append(fechaHasta);
        return sb.toString().trim();
    }

    private void exportarPdf() {
        if (listaActual.isEmpty()) {
            Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Plantacion> copia = new ArrayList<>(listaActual);
        String subtitulo = construirSubtitulo();
        Toast.makeText(this, "Generando PDF…", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                File pdf = reporteGenerator.generarReportePlantacionesFiltrado(copia, subtitulo);
                runOnUiThread(() -> compartirArchivo(pdf, "application/pdf",
                        "Compartir PDF de plantaciones"));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this,
                        "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void exportarExcel() {
        if (listaActual.isEmpty()) {
            Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Plantacion> copia = new ArrayList<>(listaActual);
        String subtitulo = construirSubtitulo();
        Toast.makeText(this, "Generando Excel…", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                File xls = excelGenerator.generarExcelPlantaciones(copia, subtitulo);
                runOnUiThread(() -> compartirArchivo(xls,
                        "application/vnd.ms-excel",
                        "Compartir Excel de plantaciones"));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this,
                        "Error al generar Excel: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void compartirArchivo(File file, String mimeType, String chooserTitle) {
        Uri uri = FileProvider.getUriForFile(
                this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, chooserTitle));
    }

    // ── CRUD ───────────────────────────────────────────────────

    private void abrirFormulario(Plantacion plantacion) {
        Intent intent = new Intent(this, FormPlantacionActivity.class);
        if (plantacion != null) {
            intent.putExtra(FormPlantacionActivity.EXTRA_PLANTACION_ID, plantacion.getId());
        }
        startActivityForResult(intent, REQUEST_FORM);
    }

    @Override
    public void onEditar(Plantacion plantacion) {
        abrirFormulario(plantacion);
    }

    @Override
    public void onEliminar(Plantacion plantacion) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar plantación")
                .setMessage("¿Estás seguro de que querés eliminar \""
                        + plantacion.getNombre() + "\"?\n"
                        + "Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    plantacionDAO.eliminar(plantacion.getId());
                    usuarioDAO.registrarAcceso(
                            sessionManager.getUsuarioId(),
                            "ELIMINAR_PLANTACION",
                            "Plantación eliminada: " + plantacion.getNombre()
                    );
                    cargarPlantaciones();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FORM && resultCode == RESULT_OK) {
            cargarPlantaciones();
        }
    }
}
