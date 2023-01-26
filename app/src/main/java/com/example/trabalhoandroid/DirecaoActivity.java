package com.example.trabalhoandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;
import java.util.List;

public class DirecaoActivity extends AppCompatActivity {

    private LocationManager locationManager = null;
    private MyLocationListener locationListener = null;
    private TextView distancia = null;
    private final static String[] permissoes = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int minTimeMS = 50;
    private static final int minDistMetros = 1;
    private static final int locationCode = 2;
    private static final double latitudePortao = -22.809215000000002;
    private static final double longitudePortao = -43.369333333333333;
    private Location distFinal = new Location("Ponto de chegada");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direcao);
        distFinal.setLatitude(latitudePortao);
        distFinal.setLongitude(longitudePortao);
        distancia = findViewById(R.id.textDistancia);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DirecaoActivity.this, permissoes, locationCode);
        }

        locationListener =  new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMS, minDistMetros, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            locationListener.onLocationChanged(location);
        }

        Button botaoVoltar = findViewById(R.id.button_voltar);
        botaoVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissoes, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissoes, grantResults);
        if (requestCode == locationCode) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(DirecaoActivity.this, "Não é possível usar esse aplicativo sem a permissão de localização", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(DirecaoActivity.this, "Permissão de localização concedida", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class MyLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(@NonNull Location location) {
            double distance = distFinal.distanceTo(location);
            String gps_info = String.format("Distancia de %.1f metro(s)", distance);
            distancia.setText(gps_info);
        }

        @Override
        public void onLocationChanged(@NonNull List<Location> locations) {
            LocationListener.super.onLocationChanged(locations);
        }

        @Override
        public void onFlushComplete(int requestCode) {
            LocationListener.super.onFlushComplete(requestCode);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            LocationListener.super.onStatusChanged(provider, status, extras);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            LocationListener.super.onProviderEnabled(provider);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            LocationListener.super.onProviderDisabled(provider);
        }
    }
}