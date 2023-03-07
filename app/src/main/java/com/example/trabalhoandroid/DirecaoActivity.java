package com.example.trabalhoandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class DirecaoActivity extends AppCompatActivity {

    private LocationManager locationManager = null;
    private MyLocationListener locationListener = null;
    private SensorEventListener sensorListener = null;
    private TextView distancia = null;
    private final static String[] permissoes = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int minTimeMS = 50;
    private static final int minDistMetros = 1;
    private static final int locationCode = 2;
    private Location distFinal = new Location("Ponto de chegada");
    private static final int raio = 5;
    private String resultadoHttp;
    private float[] matrizGravidade     = new float[3];
    private float[] matrizGeoMagnetica  = new float[3];
    private float[] matrizOrientacao    = new float[3];
    private float[] matrizRotacao       = new float[9];
    private ImageView imagem;
    private float anguloPontoDestino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direcao);

        //------------------------------------ Requisição ------------------------------------\\
        try {
            resultadoHttp = new HttpService().execute().get();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            JSONObject resposta = new JSONObject(resultadoHttp);
            distFinal.setLatitude(resposta.getDouble("lat"));
            distFinal.setLongitude(resposta.getDouble("lon"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //---------------------------------- Fim Requisição ----------------------------------\\
        distancia = findViewById(R.id.textDistancia);
        imagem = findViewById(R.id.seta);


        //------------------------------ Permissão Localização -------------------------------\\
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DirecaoActivity.this, permissoes, locationCode);
        }
        //---------------------------- Fim Permissão Localização -----------------------------\\



        //----------------------------- Atualização da Distância -----------------------------\\
        locationListener =  new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMS, minDistMetros, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        anguloPontoDestino = distFinal.bearingTo(location);
        if (location != null) {
            locationListener.onLocationChanged(location);
        }
        //--------------------------- Fim Atualização da Distância ---------------------------\\



        //----------------------------- Atualização dos Sensores -----------------------------\\
        sensorListener = new Sensores();
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor magnetField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(sensorListener, magnetField, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //--------------------------- Fim Atualização dos Sensores ---------------------------\\



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

    //Classe que irá atualizar a distância até o destino
    private class MyLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(@NonNull Location location) {
            double distance = distFinal.distanceTo(location);
            String auxDist = String.format("%.1f",distance);
            auxDist = auxDist.replace(',','.');
            double dist = Double.parseDouble(auxDist);

            if(dist < raio){
                startActivity(new Intent(DirecaoActivity.this, DestinoActivity.class));
            }

            String gps_info = String.format("Distancia de %.1f metro(s)", distance);
            distancia.setText(gps_info);
            anguloPontoDestino = distFinal.bearingTo(location);
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

    //Classe para fazer a requisição https
    public class HttpService extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder resposta = new StringBuilder();

            try {
                URL url = new URL("https://www.dcc.ufrrj.br/~marcelo/android/im-gps.php");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.connect();

                Scanner scanner = new Scanner(url.openStream());
                while (scanner.hasNext()) {
                    resposta.append(scanner.next());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return resposta.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            resultadoHttp = result;
        }
    }

    private class Sensores implements SensorEventListener{

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                matrizGravidade = event.values;
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                matrizGeoMagnetica = event.values;
            }

            if (matrizGravidade != null && matrizGeoMagnetica != null) {
                float I[] = new float[9];

                if(SensorManager.getRotationMatrix(matrizRotacao, I, matrizGravidade, matrizGeoMagnetica) == true){
                    SensorManager.getOrientation(matrizRotacao, matrizOrientacao);
                    float angulo = (float) (-matrizOrientacao[0]*180/3.14159);
                    imagem.setRotation(angulo + 180 + anguloPontoDestino);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}