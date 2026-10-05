package com.ecogestion.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.ecogestion.app.R;
import com.ecogestion.app.utils.ReporteGenerator;
import com.ecogestion.app.utils.RolHelper;
import com.ecogestion.app.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.io.File;

public class ReportesActivity extends AppCompatActivity {

    private ReporteGenerator generator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);

        String rol = new SessionManager(this).getRol();
        if (!RolHelper.puedeVerReportes(rol)) {
            Toast.makeText(this, "Tu rol no tiene acceso a reportes", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        generator = new ReporteGenerator(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btnReporteGeneral).setOnClickListener(v ->
                generarYCompartir(() -> generator.generarResumenGeneral(), "Resumen General"));

        findViewById(R.id.btnReportePlantaciones).setOnClickListener(v ->
                generarYCompartir(() -> generator.generarReportePlantaciones(), "Plantaciones"));

        findViewById(R.id.btnReporteTareas).setOnClickListener(v ->
                generarYCompartir(() -> generator.generarReporteTareas(), "Tareas"));

        findViewById(R.id.btnReporteZonas).setOnClickListener(v ->
                generarYCompartir(() -> generator.generarReporteZonas(), "Zonas"));

        findViewById(R.id.btnReporteGrafico).setOnClickListener(v ->
                startActivity(new Intent(this, ReporteGraficoActivity.class)));
    }

    private void generarYCompartir(GeneradorPdf generador, String nombre) {
        // Deshabilitar botones mientras genera
        setButtonsEnabled(false);
        Toast.makeText(this, "Generando reporte…", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                File pdf = generador.generar();
                runOnUiThread(() -> {
                    setButtonsEnabled(true);
                    compartirPdf(pdf, nombre);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setButtonsEnabled(true);
                    Toast.makeText(this,
                            "Error al generar el reporte: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void compartirPdf(File pdf, String nombre) {
        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                pdf);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Reporte EcoGestión — " + nombre);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Compartir reporte"));
    }

    private void setButtonsEnabled(boolean enabled) {
        int[] ids = {
                R.id.btnReporteGeneral,
                R.id.btnReportePlantaciones,
                R.id.btnReporteTareas,
                R.id.btnReporteZonas,
                R.id.btnReporteGrafico
        };
        for (int id : ids) {
            View v = findViewById(id);
            if (v != null) v.setEnabled(enabled);
        }
    }

    // Interfaz funcional para pasar el método de generación
    interface GeneradorPdf {
        File generar() throws Exception;
    }
}
