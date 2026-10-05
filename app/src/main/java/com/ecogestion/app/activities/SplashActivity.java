package com.ecogestion.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.ecogestion.app.MainActivity;
import com.ecogestion.app.R;
import com.ecogestion.app.database.DatabaseHelper;
import com.ecogestion.app.utils.DataSeeder;
import com.ecogestion.app.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURACION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Sembrar datos de prueba en background (solo si la BD está vacía)
        new Thread(() -> DataSeeder.sembrarSiEsNecesario(
                DatabaseHelper.getInstance(getApplicationContext()))).start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager session = new SessionManager(this);
            Intent intent;
            if (session.estaLogueado()) {
                intent = new Intent(this, MainActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, SPLASH_DURACION);
    }
}
