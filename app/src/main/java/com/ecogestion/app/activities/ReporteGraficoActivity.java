package com.ecogestion.app.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ecogestion.app.R;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.EspecieDAO;
import com.ecogestion.app.database.dao.PlantacionDAO;
import com.ecogestion.app.database.dao.ZonaDAO;
import com.ecogestion.app.models.Especie;
import com.ecogestion.app.models.Plantacion;
import com.ecogestion.app.models.Zona;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReporteGraficoActivity extends AppCompatActivity {

    private static final SimpleDateFormat FMT_DIA  =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "AR"));
    private static final SimpleDateFormat FMT_MES  =
            new SimpleDateFormat("MM/yyyy",    new Locale("es", "AR"));
    private static final SimpleDateFormat FMT_MES_LABEL =
            new SimpleDateFormat("MMM yy",     new Locale("es", "AR"));

    // ── Colores ────────────────────────────────────────────────
    private static final int[] COLORES = {
            Color.parseColor("#1A5EA6"), Color.parseColor("#4CAF50"),
            Color.parseColor("#FF8F00"), Color.parseColor("#E53935"),
            Color.parseColor("#26C6DA"), Color.parseColor("#8E24AA"),
            Color.parseColor("#00897B"), Color.parseColor("#F06292")
    };

    // ── Views ──────────────────────────────────────────────────
    private BarChart  barChart;
    private LineChart lineChart;
    private PieChart  pieChart;
    private TextView  tvSubtituloBarras, tvSubtituloLineas, tvSubtituloPie;
    private TextView  tvSinDatosBarras,  tvSinDatosLineas,  tvSinDatosPie;
    private TextView  tvTotalPlantaciones, tvTotalArboles;
    private MaterialButton btnDesde, btnHasta;
    private Spinner   spinnerZona, spinnerEspecie;

    // ── DAO ────────────────────────────────────────────────────
    private PlantacionDAO plantacionDAO;
    private List<Zona>    listaZonas    = new ArrayList<>();
    private List<Especie> listaEspecies = new ArrayList<>();

    // ── Filtros activos ────────────────────────────────────────
    private String fechaDesde = "";
    private String fechaHasta = "";

    // ══════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte_grafico);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        plantacionDAO = new PlantacionDAO(db);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Referencias
        barChart             = findViewById(R.id.barChart);
        lineChart            = findViewById(R.id.lineChart);
        pieChart             = findViewById(R.id.pieChart);
        tvSubtituloBarras    = findViewById(R.id.tvSubtituloBarras);
        tvSubtituloLineas    = findViewById(R.id.tvSubtituloLineas);
        tvSubtituloPie       = findViewById(R.id.tvSubtituloPie);
        tvSinDatosBarras     = findViewById(R.id.tvSinDatosBarras);
        tvSinDatosLineas     = findViewById(R.id.tvSinDatosLineas);
        tvSinDatosPie        = findViewById(R.id.tvSinDatosPie);
        tvTotalPlantaciones  = findViewById(R.id.tvTotalPlantaciones);
        tvTotalArboles       = findViewById(R.id.tvTotalArboles);
        btnDesde             = findViewById(R.id.btnGraficoDesde);
        btnHasta             = findViewById(R.id.btnGraficoHasta);
        spinnerZona          = findViewById(R.id.spinnerGraficoZona);
        spinnerEspecie       = findViewById(R.id.spinnerGraficoEspecie);

        cargarSpinners(db);

        btnDesde.setOnClickListener(v -> mostrarDatePicker(true));
        btnHasta.setOnClickListener(v -> mostrarDatePicker(false));
        findViewById(R.id.btnGraficoAplicar).setOnClickListener(v -> cargarGraficos());
        findViewById(R.id.btnGraficoLimpiar).setOnClickListener(v -> limpiarFiltros());

        configurarCharts();
        cargarGraficos();
    }

    // ── Spinners ───────────────────────────────────────────────

    private void cargarSpinners(DatabaseHelper db) {
        listaZonas    = new ZonaDAO(db).obtenerTodas();
        listaEspecies = new EspecieDAO(db).obtenerTodas();

        List<String> zonas = new ArrayList<>();
        zonas.add("Todas las zonas");
        for (Zona z : listaZonas) zonas.add(z.getNombre());
        ArrayAdapter<String> adZ = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, zonas);
        adZ.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerZona.setAdapter(adZ);

        List<String> especies = new ArrayList<>();
        especies.add("Todas las especies");
        for (Especie e : listaEspecies) especies.add(e.getNombre());
        ArrayAdapter<String> adE = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, especies);
        adE.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEspecie.setAdapter(adE);
    }

    private int zonaIdSeleccionada() {
        int pos = spinnerZona.getSelectedItemPosition();
        return (pos > 0 && pos <= listaZonas.size()) ? listaZonas.get(pos - 1).getId() : 0;
    }

    private int especieIdSeleccionada() {
        int pos = spinnerEspecie.getSelectedItemPosition();
        return (pos > 0 && pos <= listaEspecies.size()) ? listaEspecies.get(pos - 1).getId() : 0;
    }

    // ── DatePicker ─────────────────────────────────────────────

    private void mostrarDatePicker(boolean esDesde) {
        Calendar cal = Calendar.getInstance();
        String actual = esDesde ? fechaDesde : fechaHasta;
        if (actual != null && !actual.isEmpty()) {
            try { cal.setTime(FMT_DIA.parse(actual)); } catch (ParseException ignored) { }
        }
        new DatePickerDialog(this, (view, y, m, d) -> {
            String fecha = String.format(new Locale("es","AR"), "%02d/%02d/%04d", d, m + 1, y);
            if (esDesde) { fechaDesde = fecha; btnDesde.setText("Desde: " + fecha); }
            else         { fechaHasta = fecha; btnHasta.setText("Hasta: " + fecha); }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void limpiarFiltros() {
        fechaDesde = ""; fechaHasta = "";
        btnDesde.setText("Desde"); btnHasta.setText("Hasta");
        spinnerZona.setSelection(0); spinnerEspecie.setSelection(0);
        cargarGraficos();
    }

    // ── Configuración visual de los charts ─────────────────────

    private void configurarCharts() {
        // Bar chart
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setFitBars(true);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setGridColor(0xFFEEEEEE);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setGranularity(1f);
        barChart.getLegend().setEnabled(false);

        // Line chart
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(true);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setGranularity(1f);
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisLeft().setGridColor(0xFFEEEEEE);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getLegend().setEnabled(false);

        // Pie chart
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(42f);
        pieChart.setTransparentCircleRadius(48f);
        pieChart.setDrawEntryLabels(false);   // Labels en la leyenda, no encima de slices
        pieChart.setUsePercentValues(true);
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        pieChart.getLegend().setTextSize(11f);
        pieChart.getLegend().setWordWrapEnabled(true);
    }

    // ── Carga y distribución de datos ──────────────────────────

    private void cargarGraficos() {
        int zonaId    = zonaIdSeleccionada();
        int especieId = especieIdSeleccionada();

        List<Plantacion> lista = plantacionDAO.obtenerFiltradas(
                zonaId, especieId, fechaDesde, fechaHasta);

        // Resumen
        int totalArboles = 0;
        for (Plantacion p : lista) totalArboles += p.getCantidadArboles();
        tvTotalPlantaciones.setText(String.valueOf(lista.size()));
        tvTotalArboles.setText(String.valueOf(totalArboles));

        String periodo = construirEtiquetaPeriodo();
        tvSubtituloBarras.setText(periodo);
        tvSubtituloPie.setText(periodo);

        dibujarBarrasPorMes(lista);
        dibujarLineasEvolucion(lista);
        dibujarTortaPorZona(lista);
    }

    private String construirEtiquetaPeriodo() {
        if (fechaDesde.isEmpty() && fechaHasta.isEmpty()) return "Todo el período";
        if (!fechaDesde.isEmpty() && !fechaHasta.isEmpty())
            return "Del " + fechaDesde + " al " + fechaHasta;
        if (!fechaDesde.isEmpty()) return "Desde " + fechaDesde;
        return "Hasta " + fechaHasta;
    }

    // ── Gráfico 1: Barras — plantines plantados por mes ────────

    private void dibujarBarrasPorMes(List<Plantacion> lista) {
        // Agrupar cantidad de árboles por "MM/yyyy", ordenado cronológicamente
        Map<String, Integer> arbolesXMes = new LinkedHashMap<>();
        for (Plantacion p : lista) {
            String clave = mesDeFecha(p.getFechaPlantacion());
            if (clave == null) continue;
            arbolesXMes.put(clave, arbolesXMes.getOrDefault(clave, 0) + p.getCantidadArboles());
        }

        // Ordenar por fecha
        List<String> claves = new ArrayList<>(arbolesXMes.keySet());
        claves.sort((a, b) -> {
            try {
                return FMT_MES.parse(a).compareTo(FMT_MES.parse(b));
            } catch (ParseException e) { return 0; }
        });

        if (claves.isEmpty()) {
            barChart.setVisibility(View.GONE);
            tvSinDatosBarras.setVisibility(View.VISIBLE);
            return;
        }
        barChart.setVisibility(View.VISIBLE);
        tvSinDatosBarras.setVisibility(View.GONE);

        List<BarEntry> entries = new ArrayList<>();
        List<String>   labels  = new ArrayList<>();
        for (int i = 0; i < claves.size(); i++) {
            String clave = claves.get(i);
            entries.add(new BarEntry(i, arbolesXMes.get(clave)));
            labels.add(labelMes(clave));
        }

        BarDataSet ds = new BarDataSet(entries, "Plantines por mes");
        ds.setColor(Color.parseColor("#1A5EA6"));
        ds.setValueTextSize(9f);
        ds.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) { return (int) v == 0 ? "" : String.valueOf((int) v); }
        });

        BarData data = new BarData(ds);
        data.setBarWidth(0.55f);
        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.getXAxis().setLabelRotationAngle(labels.size() > 5 ? -40f : 0f);
        barChart.animateY(700);
        barChart.invalidate();
    }

    // ── Gráfico 2: Líneas — evolución de plantaciones ──────────

    private void dibujarLineasEvolucion(List<Plantacion> lista) {
        // Contar plantaciones (no árboles) por mes, acumulado
        Map<String, Integer> contXMes = new LinkedHashMap<>();
        for (Plantacion p : lista) {
            String clave = mesDeFecha(p.getFechaPlantacion());
            if (clave == null) continue;
            contXMes.put(clave, contXMes.getOrDefault(clave, 0) + 1);
        }

        List<String> claves = new ArrayList<>(contXMes.keySet());
        claves.sort((a, b) -> {
            try { return FMT_MES.parse(a).compareTo(FMT_MES.parse(b)); }
            catch (ParseException e) { return 0; }
        });

        if (claves.isEmpty()) {
            lineChart.setVisibility(View.GONE);
            tvSinDatosLineas.setVisibility(View.VISIBLE);
            return;
        }
        lineChart.setVisibility(View.VISIBLE);
        tvSinDatosLineas.setVisibility(View.GONE);

        List<Entry>  entries    = new ArrayList<>();
        List<String> labels     = new ArrayList<>();
        int          acumulado  = 0;
        for (int i = 0; i < claves.size(); i++) {
            acumulado += contXMes.get(claves.get(i));
            entries.add(new Entry(i, acumulado));
            labels.add(labelMes(claves.get(i)));
        }

        LineDataSet ds = new LineDataSet(entries, "Acumulado");
        ds.setColor(Color.parseColor("#4CAF50"));
        ds.setCircleColor(Color.parseColor("#4CAF50"));
        ds.setCircleHoleColor(Color.WHITE);
        ds.setLineWidth(2.5f);
        ds.setCircleRadius(4f);
        ds.setDrawValues(true);
        ds.setValueTextSize(9f);
        ds.setValueTextColor(Color.parseColor("#3D3D3D"));
        ds.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) { return String.valueOf((int) v); }
        });
        // Relleno bajo la curva
        ds.setDrawFilled(true);
        ds.setFillColor(Color.parseColor("#4CAF50"));
        ds.setFillAlpha(40);
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData data = new LineData(ds);
        lineChart.setData(data);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.getXAxis().setLabelCount(labels.size());
        lineChart.getXAxis().setLabelRotationAngle(labels.size() > 5 ? -40f : 0f);
        lineChart.animateX(800);
        lineChart.invalidate();
    }

    // ── Gráfico 3: Torta — actividades por zona ─────────────────

    private void dibujarTortaPorZona(List<Plantacion> lista) {
        Map<String, Integer> contXZona = new LinkedHashMap<>();
        for (Plantacion p : lista) {
            String zona = p.getZonaNombre() != null ? p.getZonaNombre() : "Sin zona";
            contXZona.put(zona, contXZona.getOrDefault(zona, 0) + 1);
        }

        if (contXZona.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            tvSinDatosPie.setVisibility(View.VISIBLE);
            return;
        }
        pieChart.setVisibility(View.VISIBLE);
        tvSinDatosPie.setVisibility(View.GONE);

        List<PieEntry>  entries = new ArrayList<>();
        List<Integer>   colors  = new ArrayList<>();
        int idx = 0;
        for (Map.Entry<String, Integer> e : contXZona.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
            colors.add(COLORES[idx % COLORES.length]);
            idx++;
        }

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(colors);
        ds.setSliceSpace(2f);
        ds.setValueTextSize(11f);
        ds.setValueTextColor(Color.WHITE);
        ds.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) {
                return String.format(Locale.getDefault(), "%.1f%%", v);
            }
        });

        PieData data = new PieData(ds);
        pieChart.setData(data);
        pieChart.setCenterText("Zonas");
        pieChart.setCenterTextSize(13f);
        pieChart.setCenterTextColor(Color.parseColor("#3D3D3D"));
        pieChart.animateY(700);
        pieChart.invalidate();
    }

    // ── Helpers de fechas ──────────────────────────────────────

    /** Extrae "MM/yyyy" de una fecha "dd/MM/yyyy". Retorna null si no puede parsear. */
    private String mesDeFecha(String fecha) {
        if (fecha == null || fecha.isEmpty()) return null;
        try {
            return FMT_MES.format(FMT_DIA.parse(fecha));
        } catch (ParseException e) {
            return null;
        }
    }

    /** Convierte "MM/yyyy" a etiqueta legible como "Ene 24". */
    private String labelMes(String claveMes) {
        try {
            return FMT_MES_LABEL.format(FMT_MES.parse(claveMes));
        } catch (ParseException e) {
            return claveMes;
        }
    }
}
