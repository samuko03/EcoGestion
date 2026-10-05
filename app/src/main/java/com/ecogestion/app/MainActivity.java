package com.ecogestion.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ecogestion.app.activities.AuditoriaActivity;
import com.ecogestion.app.activities.LoginActivity;
import com.ecogestion.app.activities.EspeciesActivity;
import com.ecogestion.app.activities.ZonasActivity;
import com.ecogestion.app.activities.UsuariosActivity;
import com.ecogestion.app.activities.PlantacionesActivity;
import com.ecogestion.app.activities.TareasActivity;
import com.ecogestion.app.activities.ReportesActivity;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.database.dao.EspecieDAO;
import com.ecogestion.app.database.dao.PlantacionDAO;
import com.ecogestion.app.database.dao.TareaDAO;
import com.ecogestion.app.database.dao.ZonaDAO;
import com.ecogestion.app.utils.RolHelper;
import com.ecogestion.app.utils.SessionManager;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        configurarBienvenida(navigationView);
        aplicarPermisosMenu(navigationView);
        configurarBotonesRapidos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarContadores();
    }

    private void aplicarPermisosMenu(NavigationView navigationView) {
        String rol = sessionManager.getRol();
        android.view.Menu menu = navigationView.getMenu();

        // Usuarios y Auditoría: solo ADMIN
        menu.findItem(R.id.nav_usuarios).setVisible(RolHelper.puedeVerMenuUsuarios(rol));
        menu.findItem(R.id.nav_auditoria).setVisible(RolHelper.puedeVerMenuUsuarios(rol));

        // Reportes: todos excepto OPERADOR
        menu.findItem(R.id.nav_reportes).setVisible(RolHelper.puedeVerReportes(rol));
    }

    private void configurarBienvenida(NavigationView navigationView) {
        TextView txtBienvenida = findViewById(R.id.txtBienvenida);
        txtBienvenida.setText("Bienvenido/a, " + sessionManager.getNombre());

        android.view.View header = navigationView.getHeaderView(0);
        TextView navNombre = header.findViewById(R.id.navHeaderNombre);
        navNombre.setText(sessionManager.getNombreCompleto());

        // Mostrar rol debajo del nombre si existe el TextView
        TextView navRol = header.findViewById(R.id.navHeaderRol);
        if (navRol != null) {
            navRol.setText(RolHelper.etiqueta(sessionManager.getRol()));
        }
    }

    private void actualizarContadores() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);

        int countPlantaciones = new PlantacionDAO(db).contarTodas();
        int countZonas        = new ZonaDAO(db).obtenerTodas().size();
        int countEspecies     = new EspecieDAO(db).contarTodas();
        int countTareas       = new TareaDAO(db).contarPorEstado("PENDIENTE")
                              + new TareaDAO(db).contarPorEstado("EN_CURSO");

        ((TextView) findViewById(R.id.txtCountPlantaciones)).setText(String.valueOf(countPlantaciones));
        ((TextView) findViewById(R.id.txtCountZonas)).setText(String.valueOf(countZonas));
        ((TextView) findViewById(R.id.txtCountEspecies)).setText(String.valueOf(countEspecies));
        ((TextView) findViewById(R.id.txtCountTareas)).setText(String.valueOf(countTareas));
    }

    private void configurarBotonesRapidos() {
        findViewById(R.id.btnPlantaciones).setOnClickListener(v ->
                startActivity(new Intent(this, PlantacionesActivity.class)));
        findViewById(R.id.btnZonas).setOnClickListener(v ->
                startActivity(new Intent(this, ZonasActivity.class)));
        findViewById(R.id.btnTareas).setOnClickListener(v ->
                startActivity(new Intent(this, TareasActivity.class)));
        findViewById(R.id.btnReportes).setOnClickListener(v ->
                startActivity(new Intent(this, ReportesActivity.class)));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_plantaciones) {
            startActivity(new Intent(this, PlantacionesActivity.class));
        } else if (id == R.id.nav_zonas) {
            startActivity(new Intent(this, ZonasActivity.class));
        } else if (id == R.id.nav_especies) {
            startActivity(new Intent(this, EspeciesActivity.class));
        } else if (id == R.id.nav_tareas) {
            startActivity(new Intent(this, TareasActivity.class));
        } else if (id == R.id.nav_reportes) {
            startActivity(new Intent(this, ReportesActivity.class));
        } else if (id == R.id.nav_usuarios) {
            startActivity(new Intent(this, UsuariosActivity.class));
        } else if (id == R.id.nav_auditoria) {
            startActivity(new Intent(this, AuditoriaActivity.class));
        } else if (id == R.id.nav_cerrar_sesion) {
            cerrarSesion();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void cerrarSesion() {
        sessionManager.cerrarSesion();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
