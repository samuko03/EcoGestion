package com.ecogestion.app.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ecogestion.app.database.DatabaseHelper;

/**
 * Inserta datos de prueba realistas para Córdoba, Argentina.
 * Solo actúa si la base está prácticamente vacía (≤ 1 usuario = solo admin).
 * Llama a DataSeeder.sembrar(db) una sola vez.
 */
public class DataSeeder {

    public static void sembrarSiEsNecesario(DatabaseHelper helper) {
        SQLiteDatabase db = helper.getWritableDatabase();

        // Verificar si ya hay datos reales (más de 1 usuario)
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USUARIOS, null);
        int totalUsuarios = 0;
        if (c.moveToFirst()) totalUsuarios = c.getInt(0);
        c.close();
        if (totalUsuarios > 1) return; // ya fue sembrado

        db.beginTransaction();
        try {
            insertarUsuarios(db);
            insertarZonas(db);
            insertarEspecies(db);
            insertarPlantaciones(db);
            insertarTareas(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ══════════════════════════════════════════════════════════
    // USUARIOS  (10 usuarios + el admin que ya existe)
    // ══════════════════════════════════════════════════════════
    private static void insertarUsuarios(SQLiteDatabase db) {
        Object[][] usuarios = {
            // nombre_usuario, contrasena, nombre, apellido, email, rol, activo
            {"garcia.m",  "pass123", "Martina",   "García",    "m.garcia@ambiente.cba.gov.ar",    "SUPERVISOR",    1},
            {"lopez.r",   "pass123", "Rodrigo",   "López",     "r.lopez@ambiente.cba.gov.ar",     "OPERADOR",    1},
            {"fernandez.s","pass123","Sofía",     "Fernández", "s.fernandez@ambiente.cba.gov.ar", "OPERADOR",    1},
            {"torres.j",  "pass123", "Juan",      "Torres",    "j.torres@ambiente.cba.gov.ar",    "INSPECTOR",   1},
            {"ruiz.a",    "pass123", "Alejandro", "Ruiz",      "a.ruiz@ambiente.cba.gov.ar",      "COORDINADOR", 1},
            {"molina.v",  "pass123", "Valentina", "Molina",    "v.molina@ambiente.cba.gov.ar",    "OPERADOR",    1},
            {"romero.f",  "pass123", "Federico",  "Romero",    "f.romero@ambiente.cba.gov.ar",    "INSPECTOR",   1},
            {"diaz.l",    "pass123", "Laura",     "Díaz",      "l.diaz@ambiente.cba.gov.ar",      "SUPERVISOR",  1},
            {"medina.c",  "pass123", "Carlos",    "Medina",    "c.medina@ambiente.cba.gov.ar",    "OPERADOR",    1},
            {"perez.n",   "pass123", "Natalia",   "Pérez",     "n.perez@ambiente.cba.gov.ar",     "OPERADOR",    0},
        };
        for (Object[] u : usuarios) {
            ContentValues v = new ContentValues();
            v.put(DatabaseHelper.COL_NOMBRE_USUARIO, (String) u[0]);
            v.put(DatabaseHelper.COL_CONTRASENA,     (String) u[1]);
            v.put(DatabaseHelper.COL_NOMBRE,         (String) u[2]);
            v.put(DatabaseHelper.COL_APELLIDO,       (String) u[3]);
            v.put(DatabaseHelper.COL_EMAIL,          (String) u[4]);
            v.put(DatabaseHelper.COL_ROL,            (String) u[5]);
            v.put(DatabaseHelper.COL_ACTIVO,         (int)    u[6]);
            db.insert(DatabaseHelper.TABLE_USUARIOS, null, v);
        }
    }

    // ══════════════════════════════════════════════════════════
    // ZONAS  (10 zonas de distintos departamentos de Córdoba)
    // ══════════════════════════════════════════════════════════
    private static void insertarZonas(SQLiteDatabase db) {
        // nombre, departamento, localidad, latitud, longitud, estado, responsable_id
        Object[][] zonas = {
            {"Bosque Chaqueño Norte",    "Cruz del Eje",  "Cruz del Eje",    "-30.7279", "-64.8058", "ACTIVA",     3},
            {"Corredor Serrano Central", "Punilla",       "Cosquín",         "-31.2426", "-64.4726", "ACTIVA",     3},
            {"Ribera Río Suquía",        "Capital",       "Córdoba",         "-31.4050", "-64.1780", "ACTIVA",     8},
            {"Monte Calamuchita",        "Calamuchita",   "Villa General Belgrano", "-31.9820", "-64.5570", "ACTIVA", 5},
            {"Serranías de Pocho",       "Pocho",         "Salsacate",       "-31.3400", "-65.0900", "PENDIENTE",   5},
            {"Bosque Espinal Sur",       "Río Cuarto",    "Río Cuarto",      "-33.1307", "-64.3499", "ACTIVA",     8},
            {"Cuenca Alta San Antonio",  "San Alberto",   "Mina Clavero",    "-31.7247", "-65.0126", "ACTIVA",     3},
            {"Llanura Noreste",          "Sobremonte",    "San Francisco del Chañar", "-29.9300", "-63.9400", "PENDIENTE",   5},
            {"Sierra Grande Este",       "Ischilín",      "Deán Funes",      "-30.4220", "-64.3550", "ACTIVA",     8},
            {"Pampa de Achala",          "San Alberto",   "Achala",          "-31.5700", "-64.9100", "ACTIVA",     3},
        };
        for (Object[] z : zonas) {
            ContentValues v = new ContentValues();
            v.put(DatabaseHelper.COL_ZONA_NOMBRE,          (String) z[0]);
            v.put(DatabaseHelper.COL_ZONA_DEPARTAMENTO,    (String) z[1]);
            v.put(DatabaseHelper.COL_ZONA_LOCALIDAD,       (String) z[2]);
            v.put(DatabaseHelper.COL_ZONA_LATITUD,         (String) z[3]);
            v.put(DatabaseHelper.COL_ZONA_LONGITUD,        (String) z[4]);
            v.put(DatabaseHelper.COL_ZONA_ESTADO,          (String) z[5]);
            v.put(DatabaseHelper.COL_ZONA_RESPONSABLE_ID,  (int)    z[6]);
            db.insert(DatabaseHelper.TABLE_ZONAS, null, v);
        }
    }

    // ══════════════════════════════════════════════════════════
    // ESPECIES  (12 especies nativas de Córdoba)
    // ══════════════════════════════════════════════════════════
    private static void insertarEspecies(SQLiteDatabase db) {
        // nombre, nombre_cientifico, ecorregion, cantidad_disponible, descripcion
        Object[][] especies = {
            {"Quebracho Blanco",  "Aspidosperma quebracho-blanco", "Chaco Seco",         1200, "Árbol emblemático del Chaco. Madera muy dura. Tolera sequías extremas."},
            {"Quebracho Colorado","Schinopsis lorentzii",           "Chaco Seco",          850, "Alta resistencia. Madera pesada y muy valorada. Especie en recuperación."},
            {"Algarrobo Blanco",  "Prosopis alba",                  "Monte Espinal",      2000, "Multipropósito: sombra, frutos comestibles, fijación de nitrógeno."},
            {"Algarrobo Negro",   "Prosopis nigra",                 "Monte Espinal",      1750, "Frutos en vainas dulces. Excelente para fauna silvestre y apicultura."},
            {"Tala",              "Celtis ehrenbergiana",           "Espinal",             600, "Pioneer en recuperación de bosques degradados. Muy resistente."},
            {"Molle de Beber",    "Lithraea molleoides",            "Serrano",            980, "Especie serrana. Resina medicinal. Atrae polinizadores nativos."},
            {"Coco",              "Ziziphus mistol",                "Chaco Seco",          430, "Frutos dulces valorados por fauna y comunidades locales."},
            {"Chañar",            "Geoffroea decorticans",          "Monte / Espinal",     770, "Tolera salinidad. Frutos usados para arrope. Buena cobertura."},
            {"Caldén",            "Prosopis caldenia",              "Caldén",              540, "Especie característica de la región pampeana semiárida."},
            {"Piquillín",         "Condalia microphylla",           "Monte Espinal",       310, "Arbusto-árbol. Refugio de fauna. Frutos apreciados por aves."},
            {"Espinillo",         "Vachellia caven",                "Espinal",            1100, "Nodulación de nitrógeno. Flores aromáticas. Rápido crecimiento."},
            {"Horco Quebracho",   "Schinopsis haenkeana",           "Serrano",             260, "Especie de monte serrano. Madera dura. Endémica del centro de Argentina."},
        };
        for (Object[] e : especies) {
            ContentValues v = new ContentValues();
            v.put(DatabaseHelper.COL_ESP_NOMBRE,            (String) e[0]);
            v.put(DatabaseHelper.COL_ESP_NOMBRE_CIENTIFICO, (String) e[1]);
            v.put(DatabaseHelper.COL_ESP_ECORREGION,        (String) e[2]);
            v.put(DatabaseHelper.COL_ESP_CANTIDAD,          (int)    e[3]);
            v.put(DatabaseHelper.COL_ESP_DESCRIPCION,       (String) e[4]);
            db.insert(DatabaseHelper.TABLE_ESPECIES, null, v);
        }
    }

    // ══════════════════════════════════════════════════════════
    // PLANTACIONES  (40 plantaciones distribuidas en 18 meses)
    // ══════════════════════════════════════════════════════════
    private static void insertarPlantaciones(SQLiteDatabase db) {
        // nombre, zona_id(1-10), especie_id(1-12), cantidad, fecha(dd/MM/yyyy), estado, responsable_id, obs
        Object[][] p = {
            // ── 2023 ──────────────────────────────────────────────────────
            {"Campaña Algarrobo Otoño 2023",      1, 3,  150, "05/04/2023", "COMPLETADA", 2, "Primera campaña del año. Buenos resultados de prendimiento."},
            {"Reforestación Serrana I",           2, 6,   80, "12/04/2023", "COMPLETADA", 4, "Zona de incendio 2022. Recuperación progresiva."},
            {"Corredor Quebrachos Norte",         1, 1,  200, "20/05/2023", "COMPLETADA", 2, "Refuerzo de corridors ecológicos chaco-serrano."},
            {"Plantación Ribera Suquía A",        3, 5,  120, "08/06/2023", "COMPLETADA", 6, "Control de erosión hídrica. Taludes consolidados."},
            {"Monte Nativo Calamuchita I",        4, 2,   90, "14/06/2023", "COMPLETADA", 9, "Restauración post-incendio. Zona alta Calamuchita."},
            {"Espinal Sur — Bloque 1",            6, 3,  300, "03/07/2023", "COMPLETADA", 6, "Gran bloque en llanura. Coordinado con municipio de Río Cuarto."},
            {"Reforestación Pocho I",             5, 7,   60, "18/07/2023", "EN_PROCESO",     2, "Zona aislada. Acceso dificultoso. Monitoreo bimensual."},
            {"Campaña Invierno 2023 — Cosquín",   2, 4,  175, "22/07/2023", "COMPLETADA", 4, "Participación de escuelas locales. Alta concurrencia vecinal."},
            {"Chañar Llanura Noreste",            8, 8,  110, "10/08/2023", "EN_PROCESO",     9, "Inicio del proyecto en zona noreste. Suelos salinos."},
            {"Tala Corredor — Tramo 1",           3, 5,   95, "28/08/2023", "COMPLETADA", 6, "Primer tramo del corredor ribereño Suquía."},
            {"Algarrobo Pampa Achala I",          10,3,  220, "05/09/2023", "EN_PROCESO",     2, "Alta altitud. Condiciones ventosas. Plantines con tutores."},
            {"Sierra Grande — Horco Quebracho",   9, 12,  45, "19/09/2023", "EN_PROCESO",     4, "Especie endémica priorizada. Zona de acceso restringido."},
            {"Espinillo Mina Clavero",            7, 11, 160, "30/09/2023", "COMPLETADA", 6, "Trabajo conjunto con comunidad local. Jornada multitudinaria."},
            {"Quebracho Blanco — Bloque Sur",     6, 1,  280, "10/10/2023", "EN_PROCESO",     9, "Ampliación del bloque 1. Suelos profundos y bien drenados."},
            {"Campaña Primavera 2023 — Cruz Eje", 1, 2,  135, "25/10/2023", "EN_PROCESO",     2, "Temporada primaveral. Lluvias favorables previas."},
            {"Piquillín y Tala — Ribera",         3, 10,  70, "08/11/2023", "EN_PROCESO",     6, "Especies de matorral para consolidar bancos."},
            {"Molle Serrano — Tramo 2",           2, 6,  115, "20/11/2023", "EN_PROCESO",     4, "Continuación del trabajo en Punilla. Buen prendimiento."},
            {"Algarrobo Negro — Espinal Este",    9, 4,  190, "03/12/2023", "EN_PROCESO",     9, "Gran lote en llanos. Apoyo de productores vecinos."},
            {"Caldén Río Cuarto — Área 2",        6, 9,   85, "15/12/2023", "EN_PROCESO",     6, "Especie de caldén para recuperar fisonomía original."},
            {"Reforestación Fin de Año 2023",     7, 3,  140, "28/12/2023", "EN_PROCESO",     2, "Cierre de temporada. Plantines excedentes de vivero."},
            // ── 2024 ──────────────────────────────────────────────────────
            {"Campaña Verano 2024 — Pocho",       5, 11,  75, "10/01/2024", "EN_PROCESO",     4, "Riego de apoyo primeras semanas. Sequía persistente."},
            {"Corredor Serrano — Etapa 2",        2, 6,  205, "22/01/2024", "EN_PROCESO",     2, "Avance de 4 km del corredor planificado."},
            {"Algarrobo Blanco Achala II",       10, 3,  310, "14/02/2024", "EN_PROCESO",     9, "Mayor plantación del proyecto. Zona núcleo."},
            {"Plantación Comunitaria Cosquín",    2, 5,   90, "02/03/2024", "EN_PROCESO",     6, "Voluntarios de 3 colegios secundarios. Gran impacto social."},
            {"Tala y Chañar — Bloque Noreste",    8, 8,  125, "18/03/2024", "EN_PROCESO",     4, "Segunda etapa Sobremonte. Mejora de supervivencia 78%."},
            {"Quebracho Colorado — Eje Norte",    1, 2,  170, "05/04/2024", "EN_PROCESO",     2, "Reposición tras mortandad por helada tardía de 2023."},
            {"Espinillo Calamuchita II",          4, 11, 100, "20/04/2024", "EN_PROCESO",     9, "Bordes de picadas forestales. Cortafuegos vegetales."},
            {"Horco Quebracho — Reserva",         9, 12,  55, "02/05/2024", "EN_PROCESO",     4, "Área de reserva estricta. Solo personal técnico."},
            {"Coco Chaco — Ampliación",           1, 7,  130, "16/05/2024", "EN_PROCESO",     6, "Ampliación zona 1. Frutos para fauna silvestre nativa."},
            {"Molle Serrano Mina Clavero II",     7, 6,  185, "28/05/2024", "EN_PROCESO",     2, "Restauración ladera sur. Alta pendiente. Bancales manuales."},
            {"Campaña Junio 2024 — Río Cuarto",   6, 3,  240, "10/06/2024", "EN_PROCESO",     9, "Coordinación interinstitucional. INTA + Municipio + ONG."},
            {"Piquillín Ribera Suquía B",         3, 10,  65, "24/06/2024", "EN_PROCESO",     6, "Segundo tramo costero. Complementa plantación Nov 2023."},
            {"Caldén Pampeano Sur",               6, 9,  150, "08/07/2024", "EN_PROCESO",     4, "Restauración de pastizal degradado con caldén nativo."},
            {"Sierra Grande — Bloque 2",          9, 1,  220, "22/07/2024", "EN_PROCESO",     2, "Expansión hacia el norte de la sierra. Acceso mejorado."},
            {"Campaña Invernal 2024 — Punilla",   2, 4,  195, "05/08/2024", "EN_PROCESO",     9, "Plantación con sistema de goteo experimental."},
            {"Espinal Norte — Refuerzo",          8, 11,  88, "19/08/2024", "EN_PROCESO",     6, "Reposición de ejemplares con bajo prendimiento inicial."},
            {"Algarrobo Comunitario — Cruz Eje",  1, 3,  260, "02/09/2024", "EN_PROCESO",     4, "Festival del árbol. 200 vecinos participantes."},
            {"Tala Serrana — Tramo Final",        7, 5,  105, "16/09/2024", "EN_PROCESO",     2, "Completado el corredor de Mina Clavero. Etapa final."},
            {"Quebracho Blanco — Área Reserva",   5, 1,  175, "30/09/2024", "EN_PROCESO",     9, "Zona de exclusión ganadera. Muy alto potencial de regeneración."},
            {"Plantación Gran Escala Achala III", 10,3,  420, "14/10/2024", "EN_PROCESO",     6, "Mayor plantación del año. Registra récord de plantines."},
        };

        for (Object[] pl : p) {
            ContentValues v = new ContentValues();
            v.put(DatabaseHelper.COL_PL_NOMBRE,           (String) pl[0]);
            v.put(DatabaseHelper.COL_PL_ZONA_ID,          (int)    pl[1]);
            v.put(DatabaseHelper.COL_PL_ESPECIE_ID,       (int)    pl[2]);
            v.put(DatabaseHelper.COL_PL_CANTIDAD,         (int)    pl[3]);
            v.put(DatabaseHelper.COL_PL_FECHA_PLANTACION, (String) pl[4]);
            v.put(DatabaseHelper.COL_PL_ESTADO,           (String) pl[5]);
            v.put(DatabaseHelper.COL_PL_RESPONSABLE_ID,   (int)    pl[6]);
            v.put(DatabaseHelper.COL_PL_OBSERVACIONES,    (String) pl[7]);
            db.insert(DatabaseHelper.TABLE_PLANTACIONES, null, v);
        }
    }

    // ══════════════════════════════════════════════════════════
    // TAREAS  (25 tareas de distintos tipos, estados y prioridades)
    // ══════════════════════════════════════════════════════════
    private static void insertarTareas(SQLiteDatabase db) {
        // titulo, descripcion, tipo, estado, prioridad, zona_id, plantacion_id, asignado_id, fecha_inicio, fecha_limite, obs
        Object[][] tareas = {
            {"Riego plantines Achala — semana 1",
             "Riego de apoyo manual a plantines recién instalados.", "RIEGO", "COMPLETADA", "ALTA",
             10, 11, 6, "06/04/2023", "12/04/2023", "Completado en tiempo y forma."},

            {"Monitoreo supervivencia Quebracho Norte",
             "Relevamiento de supervivencia 30 días post-plantación.", "MONITOREO", "COMPLETADA", "ALTA",
             1, 3, 4, "20/06/2023", "20/06/2023", "Supervivencia del 82%. Dentro del rango esperado."},

            {"Reposición marras — Calamuchita I",
             "Reemplazar ejemplares muertos de la plantación junio.", "MANTENIMIENTO", "COMPLETADA", "MEDIA",
             4, 5, 9, "10/07/2023", "25/07/2023", "Se repusieron 18 de 22 marras detectadas."},

            {"Control de herbáceas invasoras — Ribera Suquía",
             "Desmalezado manual alrededor de plantines.", "CONTROL_INVASORAS", "COMPLETADA", "MEDIA",
             3, 4, 6, "15/07/2023", "30/07/2023", "Especie dominante: Ligustrum. Se eliminó mecánicamente."},

            {"Instalación tutores zona Pampa Achala",
             "Colocación de estacas y cintas en zona expuesta al viento.", "MANTENIMIENTO", "COMPLETADA", "ALTA",
             10, 11, 2, "06/09/2023", "15/09/2023", "Todos los plantines tutelados. Vientos de hasta 80 km/h."},

            {"Relevamiento fotográfico Sierra Grande",
             "Documentación del estado actual antes de segunda etapa.", "MONITOREO", "COMPLETADA", "BAJA",
             9, 12, 4, "25/09/2023", "25/09/2023", "Se tomaron 120 fotografías georreferenciadas."},

            {"Capacitación operarios — Técnicas de plantación",
             "Taller teórico-práctico para nuevo personal incorporado.", "CAPACITACION", "COMPLETADA", "MEDIA",
             null, null, 8, "02/10/2023", "02/10/2023", "Asistieron 8 operarios. Material entregado."},

            {"Control de ganado — Zona Llanura Noreste",
             "Verificar integridad del alambrado perimetral.", "CONTROL_INVASORAS", "COMPLETADA", "ALTA",
             8, 9, 9, "15/10/2023", "20/10/2023", "Se detectaron 3 postes caídos. Reparados en el momento."},

            {"Riego drip — sistema experimental Punilla",
             "Instalación de cinta de goteo en sector piloto.", "RIEGO", "EN_CURSO", "ALTA",
             2, 35, 4, "05/08/2024", "30/08/2024", "Se instaló el 60% del sistema. Continúa."},

            {"Monitoreo prendimiento — Algarrobo Achala III",
             "Primer relevamiento post-plantación octubre 2024.", "MONITOREO", "EN_CURSO", "ALTA",
             10, 40, 2, "21/10/2024", "21/11/2024", "En curso. Primeras mediciones indican 88% vivos."},

            {"Desmalezado Espinal Sur Bloque 2",
             "Control manual de pastos en zona de plantines jóvenes.", "MANTENIMIENTO", "EN_CURSO", "MEDIA",
             6, 14, 6, "25/10/2024", "15/11/2024", "50% completado. Continúa equipo de 4 personas."},

            {"Reparación cerco perimetral — Calamuchita",
             "Mantenimiento del alambrado que protege plantación.", "MANTENIMIENTO", "EN_CURSO", "MEDIA",
             4, 27, 9, "01/11/2024", "20/11/2024", "Materiales adquiridos. Trabajo en progreso."},

            {"Análisis de suelos — Cruz del Eje",
             "Extracción de muestras para laboratorio edáfico.", "MONITOREO", "PENDIENTE", "MEDIA",
             1, null, 4, "15/11/2024", "30/11/2024", "Coordinar con INTA Manfredi para análisis."},

            {"Capacitación guardaparques — Flora nativa",
             "Identificación de especies y protocolos de manejo.", "CAPACITACION", "PENDIENTE", "BAJA",
             null, null, 8, "18/11/2024", "18/11/2024", "Confirmados 12 participantes de 3 guardaparques."},

            {"Relevamiento fauna asociada",
             "Inventario de aves y mamíferos en zonas restauradas.", "MONITOREO", "PENDIENTE", "MEDIA",
             2, 22, 7, "22/11/2024", "06/12/2024", "Ornitólogo contratado para el relevamiento."},

            {"Riego de emergencia — Zona Pocho",
             "Seca inusual. Riego manual de sostén para plantines.", "RIEGO", "PENDIENTE", "ALTA",
             5, 7, 2, "10/11/2024", "17/11/2024", "URGENTE: supervivencia en riesgo por falta de lluvia."},

            {"Instalación cartelería ambiental",
             "Señalización de sitios de restauración con información educativa.", "OTRO", "PENDIENTE", "BAJA",
             3, null, 6, "25/11/2024", "10/12/2024", "Diseños ya aprobados. Pendiente impresión."},

            {"Corte de pasto — Entorno Ribera Suquía",
             "Desbrozado mecánico en perímetro de seguridad.", "MANTENIMIENTO", "PENDIENTE", "BAJA",
             3, 16, 9, "28/11/2024", "05/12/2024", "Coordinado con Aguas Cordobesas."},

            {"Evaluación anual — Informe Quebracho Norte",
             "Elaborar informe técnico de avance para la Secretaría.", "MONITOREO", "PENDIENTE", "ALTA",
             1, 15, 8, "01/12/2024", "15/12/2024", "Incluir mapas GIS actualizados y fotos comparativas."},

            {"Reposición marras masiva — Achala III",
             "500 plantines a reponer en zona de mayor mortandad.", "MANTENIMIENTO", "PENDIENTE", "ALTA",
             10, 40, 6, "05/12/2024", "20/12/2024", "Plantines reservados en vivero provincial."},

            {"Plantación complementaria — Sierra Grande",
             "Densificación de zonas con baja cobertura.", "OTRO", "PENDIENTE", "MEDIA",
             9, 34, 4, "10/12/2024", "10/12/2024", "200 plantines de quebracho blanco disponibles."},

            {"Control ligustro invasor — Punilla",
             "Corte y tratamiento con herbicida de corte en tocón.", "CONTROL_INVASORAS", "PENDIENTE", "ALTA",
             2, 17, 2, "12/12/2024", "20/12/2024", "Especie invasora con alta presión en el corredor."},

            {"Reunión técnica cierre de año",
             "Revisión de metas anuales y planificación 2025.", "CAPACITACION", "PENDIENTE", "MEDIA",
             null, null, 8, "19/12/2024", "19/12/2024", "Sede: Dirección Provincial de Bosques."},

            {"Auditoría ambiental — Zonas prioritarias",
             "Revisión de impactos no esperados en zonas sensibles.", "MONITOREO", "PENDIENTE", "MEDIA",
             null, null, 7, "20/12/2024", "27/12/2024", "Convocada por Auditoría General de la Provincia."},

            {"Planificación campaña verano 2025",
             "Elaborar plan operativo para enero-marzo 2025.", "OTRO", "PENDIENTE", "MEDIA",
             null, null, 5, "20/12/2024", "31/12/2024", "Incluir presupuesto, zonas y RR.HH. requeridos."},
        };

        for (Object[] t : tareas) {
            ContentValues v = new ContentValues();
            v.put(DatabaseHelper.COL_TAR_TITULO,        (String) t[0]);
            v.put(DatabaseHelper.COL_TAR_DESCRIPCION,   (String) t[1]);
            v.put(DatabaseHelper.COL_TAR_TIPO,          (String) t[2]);
            v.put(DatabaseHelper.COL_TAR_ESTADO,        (String) t[3]);
            v.put(DatabaseHelper.COL_TAR_PRIORIDAD,     (String) t[4]);
            if (t[5] != null) v.put(DatabaseHelper.COL_TAR_ZONA_ID,       (int) t[5]);
            if (t[6] != null) v.put(DatabaseHelper.COL_TAR_PLANTACION_ID, (int) t[6]);
            v.put(DatabaseHelper.COL_TAR_ASIGNADO_ID,  (int)    t[7]);
            v.put(DatabaseHelper.COL_TAR_FECHA_INICIO, (String) t[8]);
            v.put(DatabaseHelper.COL_TAR_FECHA_LIMITE, (String) t[9]);
            v.put(DatabaseHelper.COL_TAR_OBSERVACIONES,(String) t[10]);
            db.insert(DatabaseHelper.TABLE_TAREAS, null, v);
        }
    }
}
