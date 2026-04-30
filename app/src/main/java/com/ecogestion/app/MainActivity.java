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

import com.ecogestion.app.activities.LoginActivity;
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
        configurarBotonesRapidos();
    }

    private void configurarBienvenida(NavigationView navigationView) {
        TextView txtBienvenida = findViewById(R.id.txtBienvenida);
        txtBienvenida.setText("Bienvenido/a, " + sessionManager.getNombre());

        TextView navNombre = navigationView.getHeaderView(0).findViewById(R.id.navHeaderNombre);
        navNombre.setText(sessionManager.getNombreCompleto());
    }

    private void configurarBotonesRapidos() {
        findViewById(R.id.btnPlantaciones).setOnClickListener(v ->
                Toast.makeText(this, "Módulo Plantaciones — próximamente", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnZonas).setOnClickListener(v ->
                Toast.makeText(this, "Módulo Zonas — próximamente", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnTareas).setOnClickListener(v ->
                Toast.makeText(this, "Módulo Tareas — próximamente", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnReportes).setOnClickListener(v ->
                Toast.makeText(this, "Módulo Reportes — próximamente", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_plantaciones) {
            Toast.makeText(this, "Plantaciones — próximamente", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_zonas) {
            Toast.makeText(this, "Zonas — próximamente", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_especies) {
            Toast.makeText(this, "Especies — próximamente", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_tareas) {
            Toast.makeText(this, "Tareas — próximamente", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_reportes) {
            Toast.makeText(this, "Reportes — próximamente", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_usuarios) {
            Toast.makeText(this, "Usuarios — próximamente", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_auditoria) {
            Toast.makeText(this, "Auditoría — próximamente", Toast.LENGTH_SHORT).show();
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
