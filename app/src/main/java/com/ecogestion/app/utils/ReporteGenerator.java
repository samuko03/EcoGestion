package com.ecogestion.app.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.EspecieDAO;
import com.ecogestion.app.database.dao.PlantacionDAO;
import com.ecogestion.app.database.dao.TareaDAO;
import com.ecogestion.app.database.dao.ZonaDAO;
import com.ecogestion.app.models.Plantacion;
import com.ecogestion.app.models.Tarea;
import com.ecogestion.app.models.Zona;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReporteGenerator {

    // A4 a 72 dpi
    private static final int PAGE_WIDTH  = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN      = 40;
    private static final int COL_WIDTH   = PAGE_WIDTH - MARGIN * 2;

    // Colores institucionales
    private static final int COLOR_BLUE   = Color.parseColor("#1A5EA6");
    private static final int COLOR_GREEN  = Color.parseColor("#4CAF50");
    private static final int COLOR_GRAY   = Color.parseColor("#757575");
    private static final int COLOR_LIGHT  = Color.parseColor("#F5F5F5");
    private static final int COLOR_DIVIDER= Color.parseColor("#E0E0E0");
    private static final int COLOR_RED    = Color.parseColor("#F44336");
    private static final int COLOR_ORANGE = Color.parseColor("#FF9800");

    private final Context context;
    private final DatabaseHelper db;

    // Referencia a la página actualmente abierta
    private PdfDocument.Page paginaActual = null;

    public ReporteGenerator(Context context) {
        this.context = context;
        this.db = DatabaseHelper.getInstance(context);
    }

    // ── Punto de entrada para cada tipo ────────────────────────
    public File generarResumenGeneral() throws IOException {
        PdfDocument doc = new PdfDocument();
        Canvas canvas   = nuevaPagina(doc, 1);

        int y = dibujarEncabezado(canvas, "Resumen General de Actividad");
        y = dibujarFecha(canvas, y);
        y += 20;

        // Obtener conteos
        ZonaDAO zonaDAO         = new ZonaDAO(db);
        PlantacionDAO plDAO     = new PlantacionDAO(db);
        EspecieDAO espDAO       = new EspecieDAO(db);
        TareaDAO tareaDAO       = new TareaDAO(db);

        int totalZonas        = zonaDAO.obtenerTodas().size();
        int totalPlantaciones = plDAO.contarTodas();
        int totalEspecies     = espDAO.contarTodas();
        int tareasPendientes  = tareaDAO.contarPorEstado("PENDIENTE");
        int tareasEnCurso     = tareaDAO.contarPorEstado("EN_CURSO");
        int tareasCompletadas = tareaDAO.contarPorEstado("COMPLETADA");

        y = dibujarSeccion(canvas, y, "ESTADÍSTICAS GLOBALES");
        y = dibujarStatCard(canvas, y, "Zonas registradas",    String.valueOf(totalZonas),        COLOR_BLUE);
        y = dibujarStatCard(canvas, y, "Plantaciones activas", String.valueOf(totalPlantaciones),  COLOR_GREEN);
        y = dibujarStatCard(canvas, y, "Especies catalogadas", String.valueOf(totalEspecies),      COLOR_BLUE);
        y += 16;

        y = dibujarSeccion(canvas, y, "ESTADO DE TAREAS");
        y = dibujarStatCard(canvas, y, "Pendientes",  String.valueOf(tareasPendientes),  COLOR_ORANGE);
        y = dibujarStatCard(canvas, y, "En curso",    String.valueOf(tareasEnCurso),     COLOR_BLUE);
        y = dibujarStatCard(canvas, y, "Completadas", String.valueOf(tareasCompletadas), COLOR_GREEN);

        dibujarPie(canvas);
        cerrarUltimaPagina(doc);
        return guardarPdf(doc, "reporte_general");
    }

    public File generarReportePlantaciones() throws IOException {
        PlantacionDAO dao       = new PlantacionDAO(db);
        List<Plantacion> lista  = dao.obtenerTodas();

        PdfDocument doc = new PdfDocument();
        Canvas canvas   = nuevaPagina(doc, 1);
        int pageNum     = 1;

        int y = dibujarEncabezado(canvas, "Reporte de Plantaciones");
        y = dibujarFecha(canvas, y);
        y += 6;

        // Resumen rápido
        int totalArboles = 0;
        for (Plantacion p : lista) totalArboles += p.getCantidadArboles();
        Paint resumen = textPaint(10, false, COLOR_GRAY);
        canvas.drawText("Total plantaciones: " + lista.size() + "   |   Total plantines: " + totalArboles,
                MARGIN, y, resumen);
        y += 18;

        String[] cols   = {"Nombre", "Zona", "Especie", "Cant.", "Estado", "Fecha", "Responsable"};
        int[]    widths = {100, 80, 80, 38, 70, 65, 82};
        y = dibujarCabecераTabla(canvas, y, cols, widths);

        for (int i = 0; i < lista.size(); i++) {
            Plantacion p = lista.get(i);
            if (y > PAGE_HEIGHT - 100) {
                dibujarPie(canvas);
                pageNum++;
                canvas = nuevaPagina(doc, pageNum);
                y = MARGIN + 20;
                y = dibujarCabecераTabla(canvas, y, cols, widths);
            }
            String[] fila = {
                    truncar(p.getNombre(), 14),
                    truncar(p.getZonaNombre()      != null ? p.getZonaNombre()      : "-", 11),
                    truncar(p.getEspecieNombre()   != null ? p.getEspecieNombre()   : "-", 11),
                    String.valueOf(p.getCantidadArboles()),
                    p.getEstado() != null ? p.getEstado() : "-",
                    p.getFechaPlantacion() != null ? p.getFechaPlantacion() : "-",
                    truncar(p.getResponsableNombre() != null ? p.getResponsableNombre() : "-", 12)
            };
            y = dibujarFilaConDetalle(canvas, y, fila, widths, i % 2 == 0, p.getObservaciones());
        }

        if (lista.isEmpty()) {
            y = dibujarMensajeVacio(canvas, y, "No hay plantaciones registradas.");
        }

        dibujarPie(canvas);
        cerrarUltimaPagina(doc);
        return guardarPdf(doc, "reporte_plantaciones");
    }

    /** PDF con lista ya filtrada (desde PlantacionesActivity) */
    public File generarReportePlantacionesFiltrado(List<Plantacion> lista,
                                                    String subtitulo) throws IOException {
        PdfDocument doc = new PdfDocument();
        Canvas canvas   = nuevaPagina(doc, 1);
        int pageNum     = 1;

        int y = dibujarEncabezado(canvas, "Reporte de Plantaciones");
        y = dibujarFecha(canvas, y);

        if (subtitulo != null && !subtitulo.isEmpty()) {
            Paint sub = textPaint(10, false, COLOR_GRAY);
            canvas.drawText(subtitulo, MARGIN, y, sub);
            y += 16;
        }

        // Resumen con totales
        int totalArboles = 0;
        for (Plantacion p : lista) totalArboles += p.getCantidadArboles();
        Paint resumen = textPaint(10, false, COLOR_GRAY);
        canvas.drawText("Resultados: " + lista.size() + " plantaciones  |  " + totalArboles + " plantines en total",
                MARGIN, y, resumen);
        y += 18;

        String[] cols   = {"Nombre", "Zona", "Especie", "Cant.", "Estado", "Fecha", "Responsable"};
        int[]    widths = {100, 80, 80, 38, 70, 65, 82};
        y = dibujarCabecераTabla(canvas, y, cols, widths);

        for (int i = 0; i < lista.size(); i++) {
            Plantacion p = lista.get(i);
            if (y > PAGE_HEIGHT - 100) {
                dibujarPie(canvas);
                pageNum++;
                canvas = nuevaPagina(doc, pageNum);
                y = MARGIN + 20;
                y = dibujarCabecераTabla(canvas, y, cols, widths);
            }
            String[] fila = {
                    truncar(p.getNombre(), 14),
                    truncar(p.getZonaNombre()      != null ? p.getZonaNombre()      : "-", 11),
                    truncar(p.getEspecieNombre()   != null ? p.getEspecieNombre()   : "-", 11),
                    String.valueOf(p.getCantidadArboles()),
                    p.getEstado() != null ? p.getEstado() : "-",
                    p.getFechaPlantacion() != null ? p.getFechaPlantacion() : "-",
                    truncar(p.getResponsableNombre() != null ? p.getResponsableNombre() : "-", 12)
            };
            y = dibujarFilaConDetalle(canvas, y, fila, widths, i % 2 == 0, p.getObservaciones());
        }
        if (lista.isEmpty()) {
            y = dibujarMensajeVacio(canvas, y, "No hay plantaciones con los filtros seleccionados.");
        }
        dibujarPie(canvas);
        cerrarUltimaPagina(doc);
        return guardarPdf(doc, "reporte_plantaciones_filtrado");
    }

    public File generarReporteTareas() throws IOException {
        TareaDAO dao       = new TareaDAO(db);
        List<Tarea> lista  = dao.obtenerTodas();

        PdfDocument doc = new PdfDocument();
        Canvas canvas   = nuevaPagina(doc, 1);
        int pageNum     = 1;

        int y = dibujarEncabezado(canvas, "Reporte de Tareas");
        y = dibujarFecha(canvas, y);
        y += 6;

        // Resumen por estado
        int pendientes = 0, enCurso = 0, completadas = 0;
        for (Tarea t : lista) {
            switch (t.getEstado() != null ? t.getEstado() : "") {
                case "PENDIENTE":  pendientes++;  break;
                case "EN_CURSO":   enCurso++;     break;
                case "COMPLETADA": completadas++; break;
            }
        }
        Paint resumen = textPaint(10, false, COLOR_GRAY);
        canvas.drawText("Total: " + lista.size() + "   |   Pendientes: " + pendientes
                + "   En curso: " + enCurso + "   Completadas: " + completadas,
                MARGIN, y, resumen);
        y += 18;

        String[] cols   = {"Título", "Tipo", "Estado", "Prioridad", "Asignado", "F. Límite"};
        int[]    widths = {115, 75, 72, 60, 110, 68};
        y = dibujarCabecераTabla(canvas, y, cols, widths);

        for (int i = 0; i < lista.size(); i++) {
            Tarea t = lista.get(i);
            if (y > PAGE_HEIGHT - 100) {
                dibujarPie(canvas);
                pageNum++;
                canvas = nuevaPagina(doc, pageNum);
                y = MARGIN + 20;
                y = dibujarCabecераTabla(canvas, y, cols, widths);
            }
            String asig = t.getAsignadoNombre() != null && !t.getAsignadoNombre().trim().isEmpty()
                    ? t.getAsignadoNombre() : "Sin asignar";
            String[] fila = {
                    truncar(t.getTitulo(), 17),
                    t.getTipo()     != null ? t.getTipo()     : "-",
                    t.getEstado()   != null ? t.getEstado()   : "-",
                    t.getPrioridad()!= null ? t.getPrioridad(): "-",
                    truncar(asig, 15),
                    t.getFechaLimite() != null ? t.getFechaLimite() : "-"
            };
            y = dibujarFilaConDetalle(canvas, y, fila, widths, i % 2 == 0,
                    t.getDescripcion());
        }

        if (lista.isEmpty()) {
            y = dibujarMensajeVacio(canvas, y, "No hay tareas registradas.");
        }

        dibujarPie(canvas);
        cerrarUltimaPagina(doc);
        return guardarPdf(doc, "reporte_tareas");
    }

    public File generarReporteZonas() throws IOException {
        ZonaDAO zonaDAO         = new ZonaDAO(db);
        PlantacionDAO plDAO     = new PlantacionDAO(db);
        TareaDAO tareaDAO       = new TareaDAO(db);
        List<Zona> zonas        = zonaDAO.obtenerTodas();

        PdfDocument doc = new PdfDocument();
        Canvas canvas   = nuevaPagina(doc, 1);
        int pageNum     = 1;

        int y = dibujarEncabezado(canvas, "Reporte por Zona");
        y = dibujarFecha(canvas, y);
        y += 20;

        if (zonas.isEmpty()) {
            dibujarMensajeVacio(canvas, y, "No hay zonas registradas.");
            dibujarPie(canvas);
            cerrarUltimaPagina(doc);
            return guardarPdf(doc, "reporte_zonas");
        }

        List<Plantacion> todasPlantas = plDAO.obtenerTodas();
        List<Tarea>      todasTareas  = tareaDAO.obtenerTodas();

        for (Zona zona : zonas) {
            if (y > PAGE_HEIGHT - 180) {
                dibujarPie(canvas);
                pageNum++;
                canvas = nuevaPagina(doc, pageNum);
                y = MARGIN + 20;
            }

            y = dibujarSeccion(canvas, y, zona.getNombre().toUpperCase()
                    + "  —  " + zona.getDepartamento());

            // Métricas de esta zona
            int countPl = 0, totalArboles = 0, countTar = 0;
            for (Plantacion p : todasPlantas) {
                if (p.getZonaId() == zona.getId()) { countPl++; totalArboles += p.getCantidadArboles(); }
            }
            for (Tarea t : todasTareas) {
                if (t.getZonaId() == zona.getId()) countTar++;
            }

            // Localidad
            Paint paintLoc = textPaint(10, false, COLOR_GRAY);
            String locInfo = zona.getLocalidad() != null ? "Localidad: " + zona.getLocalidad() : "";
            if (zona.getLatitud() != null && !zona.getLatitud().isEmpty()) {
                locInfo += "   GPS: " + zona.getLatitud() + ", " + zona.getLongitud();
            }
            canvas.drawText(locInfo, MARGIN, y, paintLoc);
            y += 16;

            Paint paintNormal = textPaint(11, false, COLOR_GRAY);
            canvas.drawText("Plantaciones: " + countPl
                    + "   |   Plantines totales: " + totalArboles
                    + "   |   Tareas asignadas: " + countTar, MARGIN, y, paintNormal);
            y += 16;

            int estadoColor = "ACTIVA".equals(zona.getEstado())   ? COLOR_GREEN :
                              "PENDIENTE".equals(zona.getEstado()) ? COLOR_ORANGE : COLOR_GRAY;
            Paint paintEstado = textPaint(11, true, estadoColor);
            canvas.drawText("Estado: " + (zona.getEstado() != null ? zona.getEstado() : "-"),
                    MARGIN, y, paintEstado);
            y += 16;

            // Listar plantaciones de la zona (hasta 5)
            int shown = 0;
            for (Plantacion p : todasPlantas) {
                if (p.getZonaId() != zona.getId()) continue;
                if (shown >= 5) {
                    Paint mas = textPaint(9, false, COLOR_GRAY);
                    canvas.drawText("  ... y " + (countPl - 5) + " más", MARGIN + 8, y, mas);
                    y += 12;
                    break;
                }
                Paint pl = textPaint(9, false, Color.parseColor("#3D3D3D"));
                String esp = p.getEspecieNombre() != null ? p.getEspecieNombre() : "";
                canvas.drawText("  • " + truncar(p.getNombre(), 30)
                        + "  (" + esp + " · " + p.getCantidadArboles() + " plant. · " + p.getEstado() + ")",
                        MARGIN + 4, y, pl);
                y += 13;
                shown++;
            }

            y += 6;
            // Línea divisora
            Paint divPaint = new Paint();
            divPaint.setColor(COLOR_DIVIDER);
            divPaint.setStrokeWidth(1f);
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, divPaint);
            y += 14;
        }

        dibujarPie(canvas);
        cerrarUltimaPagina(doc);
        return guardarPdf(doc, "reporte_zonas");
    }

    // ── Helpers de dibujo ──────────────────────────────────────

    /** Cierra la página anterior (si existe) y abre una nueva. */
    private Canvas nuevaPagina(PdfDocument doc, int number) {
        if (paginaActual != null) {
            doc.finishPage(paginaActual);
            paginaActual = null;
        }
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(
                PAGE_WIDTH, PAGE_HEIGHT, number).create();
        paginaActual = doc.startPage(info);
        Canvas c = paginaActual.getCanvas();
        // Fondo blanco
        Paint bg = new Paint();
        bg.setColor(Color.WHITE);
        c.drawRect(0, 0, PAGE_WIDTH, PAGE_HEIGHT, bg);
        return c;
    }

    /** Cierra la última página abierta antes de guardar. */
    private void cerrarUltimaPagina(PdfDocument doc) {
        if (paginaActual != null) {
            doc.finishPage(paginaActual);
            paginaActual = null;
        }
    }

    private int dibujarEncabezado(Canvas c, String titulo) {
        // Barra superior
        Paint barPaint = new Paint();
        barPaint.setColor(COLOR_BLUE);
        c.drawRect(0, 0, PAGE_WIDTH, 70, barPaint);

        // Logo texto
        Paint logo = textPaint(18, true, Color.WHITE);
        c.drawText("EcoGestión", MARGIN, 30, logo);

        Paint sub = textPaint(10, false, Color.parseColor("#B3CFED"));
        c.drawText("Secretaría de Ambiente y Economía Circular · Córdoba", MARGIN, 48, sub);

        // Título del reporte
        Paint tit = textPaint(16, true, COLOR_BLUE);
        c.drawText(titulo, MARGIN, 95, tit);

        // Línea bajo el título
        Paint line = new Paint();
        line.setColor(COLOR_BLUE);
        line.setStrokeWidth(2f);
        c.drawLine(MARGIN, 102, PAGE_WIDTH - MARGIN, 102, line);

        return 118;
    }

    private int dibujarFecha(Canvas c, int y) {
        String fecha = "Generado el " +
                new SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", new Locale("es", "AR"))
                        .format(new Date());
        Paint p = textPaint(10, false, COLOR_GRAY);
        c.drawText(fecha, MARGIN, y, p);
        return y + 16;
    }

    private int dibujarSeccion(Canvas c, int y, String titulo) {
        Paint bg = new Paint();
        bg.setColor(COLOR_LIGHT);
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 22, bg);

        Paint txt = textPaint(11, true, COLOR_BLUE);
        c.drawText(titulo, MARGIN + 6, y + 15, txt);
        return y + 30;
    }

    private int dibujarStatCard(Canvas c, int y, String label, String valor, int color) {
        Paint border = new Paint();
        border.setColor(Color.parseColor("#E0E0E0"));
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(1f);
        c.drawRoundRect(new RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 36), 6, 6, border);

        Paint accent = new Paint();
        accent.setColor(color);
        c.drawRect(MARGIN, y, MARGIN + 5, y + 36, accent);

        Paint lPaint = textPaint(11, false, COLOR_GRAY);
        c.drawText(label, MARGIN + 14, y + 15, lPaint);

        Paint vPaint = textPaint(14, true, color);
        c.drawText(valor, MARGIN + 14, y + 30, vPaint);

        return y + 44;
    }

    private int dibujarCabecераTabla(Canvas c, int y, String[] cols, int[] widths) {
        Paint bg = new Paint();
        bg.setColor(COLOR_BLUE);
        c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 24, bg);

        Paint txt = textPaint(10, true, Color.WHITE);
        int x = MARGIN + 4;
        for (int i = 0; i < cols.length; i++) {
            c.drawText(cols[i], x, y + 16, txt);
            x += widths[i];
        }
        return y + 26;
    }

    private int dibujarFilaTabla(Canvas c, int y, String[] values, int[] widths, boolean par) {
        if (par) {
            Paint bg = new Paint();
            bg.setColor(COLOR_LIGHT);
            c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 22, bg);
        }
        Paint txt = textPaint(10, false, Color.parseColor("#3D3D3D"));
        int x = MARGIN + 4;
        for (int i = 0; i < values.length; i++) {
            c.drawText(values[i] != null ? values[i] : "-", x, y + 15, txt);
            x += widths[i];
        }
        // línea separadora
        Paint line = new Paint();
        line.setColor(COLOR_DIVIDER);
        line.setStrokeWidth(0.5f);
        c.drawLine(MARGIN, y + 22, PAGE_WIDTH - MARGIN, y + 22, line);
        return y + 24;
    }

    /**
     * Fila de tabla con sub-línea opcional para observaciones.
     * Retorna la nueva posición Y.
     */
    private int dibujarFilaConDetalle(Canvas c, int y, String[] values, int[] widths,
                                      boolean par, String obs) {
        boolean tieneObs = obs != null && !obs.trim().isEmpty();
        int rowH = tieneObs ? 34 : 22;

        if (par) {
            Paint bg = new Paint();
            bg.setColor(COLOR_LIGHT);
            c.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + rowH, bg);
        }

        Paint txt = textPaint(10, false, Color.parseColor("#3D3D3D"));
        int x = MARGIN + 4;
        for (int i = 0; i < values.length; i++) {
            c.drawText(values[i] != null ? values[i] : "-", x, y + 15, txt);
            x += widths[i];
        }

        if (tieneObs) {
            Paint obsPaint = textPaint(8, false, COLOR_GRAY);
            String trunc = obs.length() > 90 ? obs.substring(0, 87) + "..." : obs;
            c.drawText("Obs: " + trunc, MARGIN + 4, y + 29, obsPaint);
        }

        Paint line = new Paint();
        line.setColor(COLOR_DIVIDER);
        line.setStrokeWidth(0.5f);
        c.drawLine(MARGIN, y + rowH, PAGE_WIDTH - MARGIN, y + rowH, line);
        return y + rowH + 2;
    }

    private int dibujarMensajeVacio(Canvas c, int y, String msg) {
        Paint p = textPaint(12, false, COLOR_GRAY);
        c.drawText(msg, MARGIN, y + 20, p);
        return y + 40;
    }

    private void dibujarPie(Canvas c) {
        Paint bg = new Paint();
        bg.setColor(COLOR_LIGHT);
        c.drawRect(0, PAGE_HEIGHT - 30, PAGE_WIDTH, PAGE_HEIGHT, bg);

        Paint txt = textPaint(9, false, COLOR_GRAY);
        c.drawText("EcoGestión · Secretaría de Ambiente y Economía Circular · Provincia de Córdoba",
                MARGIN, PAGE_HEIGHT - 12, txt);
    }

    // ── Helpers ────────────────────────────────────────────────
    private Paint textPaint(int size, boolean bold, int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTextSize(size);
        p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        return p;
    }

    private String truncar(String texto, int maxChars) {
        if (texto == null) return "-";
        return texto.length() > maxChars ? texto.substring(0, maxChars - 1) + "…" : texto;
    }

    private File guardarPdf(PdfDocument doc, String nombre) throws IOException {
        File dir = new File(context.getExternalFilesDir(null), "Reportes");
        if (!dir.exists()) dir.mkdirs();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
        File file = new File(dir, nombre + "_" + timestamp + ".pdf");

        FileOutputStream fos = new FileOutputStream(file);
        try {
            doc.writeTo(fos);
        } finally {
            fos.close();
            doc.close();
        }
        return file;
    }
}
