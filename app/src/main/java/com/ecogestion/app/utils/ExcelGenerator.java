package com.ecogestion.app.utils;

import android.content.Context;

import com.ecogestion.app.models.Plantacion;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExcelGenerator {

    private final Context context;

    public ExcelGenerator(Context context) {
        this.context = context;
    }

    public File generarExcelPlantaciones(List<Plantacion> lista,
                                          String subtitulo) throws IOException {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet    sheet    = workbook.createSheet("Plantaciones");

        // ── Estilos ────────────────────────────────────────────
        HSSFCellStyle estiloTitulo = crearEstiloTitulo(workbook);
        HSSFCellStyle estiloHeader = crearEstiloHeader(workbook);
        HSSFCellStyle estiloPar    = crearEstiloPar(workbook);
        HSSFCellStyle estiloImpar  = crearEstiloImpar(workbook);
        HSSFCellStyle estiloNumero = crearEstiloNumero(workbook);

        int fila = 0;

        // ── Fila de título ─────────────────────────────────────
        HSSFRow rowTitulo = sheet.createRow(fila++);
        rowTitulo.setHeightInPoints(22);
        setCellValue(rowTitulo, 0, "EcoGestión — Reporte de Plantaciones", estiloTitulo);

        // Subtítulo con filtros aplicados
        if (subtitulo != null && !subtitulo.isEmpty()) {
            HSSFRow rowSub = sheet.createRow(fila++);
            HSSFCellStyle estiloSub = workbook.createCellStyle();
            HSSFFont fuenteSub = workbook.createFont();
            fuenteSub.setColor(HSSFColor.GREY_50_PERCENT.index);
            fuenteSub.setFontHeightInPoints((short) 10);
            estiloSub.setFont(fuenteSub);
            setCellValue(rowSub, 0, subtitulo, estiloSub);
        }

        // Fecha de generación
        HSSFRow rowFecha = sheet.createRow(fila++);
        HSSFCellStyle estiloFecha = workbook.createCellStyle();
        HSSFFont fuenteFecha = workbook.createFont();
        fuenteFecha.setColor(HSSFColor.GREY_50_PERCENT.index);
        fuenteFecha.setFontHeightInPoints((short) 9);
        estiloFecha.setFont(fuenteFecha);
        String fechaGen = "Generado el " +
                new SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", new Locale("es", "AR"))
                        .format(new Date());
        setCellValue(rowFecha, 0, fechaGen, estiloFecha);

        fila++; // fila en blanco

        // ── Cabecera de tabla ──────────────────────────────────
        String[] headers = {"#", "Nombre", "Zona", "Especie",
                "Cantidad de árboles", "Fecha plantación", "Estado",
                "Responsable", "Observaciones"};
        HSSFRow rowHeader = sheet.createRow(fila++);
        rowHeader.setHeightInPoints(18);
        for (int i = 0; i < headers.length; i++) {
            setCellValue(rowHeader, i, headers[i], estiloHeader);
        }

        // ── Datos ──────────────────────────────────────────────
        int num = 1;
        for (Plantacion p : lista) {
            HSSFRow row = sheet.createRow(fila++);
            row.setHeightInPoints(16);
            boolean par = (num % 2 == 0);
            HSSFCellStyle estiloFila = par ? estiloPar : estiloImpar;

            setCellValue(row, 0, String.valueOf(num++), estiloFila);
            setCellValue(row, 1, p.getNombre() != null ? p.getNombre() : "-", estiloFila);
            setCellValue(row, 2, p.getZonaNombre() != null ? p.getZonaNombre() : "-", estiloFila);
            setCellValue(row, 3, p.getEspecieNombre() != null ? p.getEspecieNombre() : "-", estiloFila);
            // Cantidad como número
            HSSFCellStyle estiloNum = par ? crearEstiloNumeroBase(workbook, true)
                                          : crearEstiloNumeroBase(workbook, false);
            row.createCell(4).setCellValue(p.getCantidadArboles());
            row.getCell(4).setCellStyle(estiloNum);

            setCellValue(row, 5, p.getFechaPlantacion() != null ? p.getFechaPlantacion() : "-", estiloFila);
            setCellValue(row, 6, p.getEstado() != null ? p.getEstado() : "-", estiloFila);
            setCellValue(row, 7, p.getResponsableNombre() != null ? p.getResponsableNombre() : "Sin asignar", estiloFila);
            setCellValue(row, 8, p.getObservaciones() != null ? p.getObservaciones() : "-", estiloFila);
        }

        // Fila de total
        fila++;
        HSSFRow rowTotal = sheet.createRow(fila);
        HSSFCellStyle estiloTotal = workbook.createCellStyle();
        HSSFFont fuenteTotal = workbook.createFont();
        fuenteTotal.setBold(true);
        fuenteTotal.setFontHeightInPoints((short) 10);
        estiloTotal.setFont(fuenteTotal);
        setCellValue(rowTotal, 0, "Total: " + lista.size() + " plantaciones", estiloTotal);

        // ── Ancho de columnas ──────────────────────────────────
        int[] anchos = {8, 25, 20, 20, 20, 18, 15, 22, 30};
        for (int i = 0; i < anchos.length; i++) {
            sheet.setColumnWidth(i, anchos[i] * 256);
        }

        // ── Guardar ────────────────────────────────────────────
        File dir = new File(context.getExternalFilesDir(null), "Reportes");
        if (!dir.exists()) dir.mkdirs();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
        File file = new File(dir, "plantaciones_" + timestamp + ".xls");

        FileOutputStream fos = new FileOutputStream(file);
        try {
            workbook.write(fos);
        } finally {
            fos.close();
            workbook.close();
        }

        return file;
    }

    // ── Helpers de estilo ──────────────────────────────────────

    private HSSFCellStyle crearEstiloTitulo(HSSFWorkbook wb) {
        HSSFCellStyle s = wb.createCellStyle();
        HSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        f.setColor(HSSFColor.DARK_BLUE.index);
        s.setFont(f);
        return s;
    }

    private HSSFCellStyle crearEstiloHeader(HSSFWorkbook wb) {
        HSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(HSSFColor.DARK_BLUE.index);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        HSSFFont f = wb.createFont();
        f.setBold(true);
        f.setColor(HSSFColor.WHITE.index);
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        return s;
    }

    private HSSFCellStyle crearEstiloPar(HSSFWorkbook wb) {
        HSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(HSSFColor.LIGHT_TURQUOISE.index);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        HSSFFont f = wb.createFont();
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        return s;
    }

    private HSSFCellStyle crearEstiloImpar(HSSFWorkbook wb) {
        HSSFCellStyle s = wb.createCellStyle();
        HSSFFont f = wb.createFont();
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        return s;
    }

    private HSSFCellStyle crearEstiloNumero(HSSFWorkbook wb) {
        HSSFCellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private HSSFCellStyle crearEstiloNumeroBase(HSSFWorkbook wb, boolean par) {
        HSSFCellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        if (par) {
            s.setFillForegroundColor(HSSFColor.LIGHT_TURQUOISE.index);
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        return s;
    }

    private void setCellValue(HSSFRow row, int col, String value, HSSFCellStyle style) {
        var cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        if (style != null) cell.setCellStyle(style);
    }
}
