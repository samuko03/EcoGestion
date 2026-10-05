package com.ecogestion.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.ecogestion.app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

public class SeleccionarUbicacionActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    public static final String EXTRA_LATITUD  = "latitud";
    public static final String EXTRA_LONGITUD = "longitud";

    // Centro de Córdoba capital
    private static final LatLng CORDOBA = new LatLng(-31.4201, -64.1888);
    private static final float  ZOOM_CIUDAD  = 12f;
    private static final float  ZOOM_DETALLE = 16f;

    private GoogleMap mapa;
    private Marker    marcador;
    private TextView  tvCoordenadas;
    private TextView  tvInstruccion;

    private double latSeleccionada = 0;
    private double lngSeleccionada = 0;

    private FusedLocationProviderClient fusedClient;

    // Launcher para pedir permiso de ubicación en runtime
    private final ActivityResultLauncher<String[]> permisosLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        Boolean fine = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        if (Boolean.TRUE.equals(fine)) {
                            activarCapaUbicacion();
                            irAMiUbicacion();
                        } else {
                            Toast.makeText(this,
                                    "Permiso de ubicación denegado. Podés marcar manualmente.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_ubicacion);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvCoordenadas = findViewById(R.id.tvCoordenadas);
        tvInstruccion = findViewById(R.id.tvInstruccion);

        // Si venimos de editar una plantación con ubicación previa
        latSeleccionada = getIntent().getDoubleExtra(EXTRA_LATITUD, 0);
        lngSeleccionada = getIntent().getDoubleExtra(EXTRA_LONGITUD, 0);

        // Botón "Mi ubicación"
        MaterialButton btnMiUbicacion = findViewById(R.id.btnMiUbicacion);
        btnMiUbicacion.setOnClickListener(v -> pedirUbicacion());

        // Botón confirmar
        MaterialButton btnConfirmar = findViewById(R.id.btnConfirmar);
        btnConfirmar.setOnClickListener(v -> confirmar());

        // Inicializar mapa
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // ── Mapa listo ─────────────────────────────────────────────
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;

        mapa.getUiSettings().setZoomControlsEnabled(true);
        mapa.getUiSettings().setMyLocationButtonEnabled(false); // usamos el nuestro

        // Si ya hay coordenadas previas, centrar y marcar
        if (latSeleccionada != 0 || lngSeleccionada != 0) {
            LatLng pos = new LatLng(latSeleccionada, lngSeleccionada);
            colocarMarcador(pos);
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, ZOOM_DETALLE));
        } else {
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(CORDOBA, ZOOM_CIUDAD));
        }

        // Tap en el mapa → coloca/mueve el marcador
        mapa.setOnMapClickListener(this::colocarMarcador);

        // Activar capa de mi ubicación si ya tenemos permiso
        if (tienePermisoUbicacion()) {
            activarCapaUbicacion();
        }
    }

    // ── Coloca o mueve el marcador verde ───────────────────────
    private void colocarMarcador(LatLng pos) {
        if (marcador != null) marcador.remove();

        marcador = mapa.addMarker(new MarkerOptions()
                .position(pos)
                .title("Plantación")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        latSeleccionada = pos.latitude;
        lngSeleccionada = pos.longitude;

        tvCoordenadas.setText(String.format("Lat: %.6f   Lng: %.6f",
                latSeleccionada, lngSeleccionada));
        tvInstruccion.setText("Ubicación marcada. Podés ajustar tocando otro punto.");
    }

    // ── Confirmar selección y devolver resultado ───────────────
    private void confirmar() {
        if (marcador == null) {
            Toast.makeText(this, "Tocá en el mapa para seleccionar una ubicación",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultado = new Intent();
        resultado.putExtra(EXTRA_LATITUD,  latSeleccionada);
        resultado.putExtra(EXTRA_LONGITUD, lngSeleccionada);
        setResult(RESULT_OK, resultado);
        finish();
    }

    // ── Ubicación actual ───────────────────────────────────────
    private void pedirUbicacion() {
        if (tienePermisoUbicacion()) {
            irAMiUbicacion();
        } else {
            permisosLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void irAMiUbicacion() {
        if (!tienePermisoUbicacion()) return;

        try {
            fusedClient.getLastLocation().addOnSuccessListener(this, (Location loc) -> {
                if (loc != null) {
                    LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
                    colocarMarcador(pos);
                    mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, ZOOM_DETALLE));
                } else {
                    Toast.makeText(this,
                            "No se pudo obtener la ubicación actual. Marcá manualmente.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            // Silencioso — no debería pasar si ya chequeamos el permiso
        }
    }

    @SuppressWarnings("MissingPermission")
    private void activarCapaUbicacion() {
        if (mapa != null && tienePermisoUbicacion()) {
            mapa.setMyLocationEnabled(true);
        }
    }

    private boolean tienePermisoUbicacion() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
