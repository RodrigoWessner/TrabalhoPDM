package com.example.trabalhopdm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

public class NovoRegistro extends AppCompatActivity implements LocationListener {
    String id, email, nome, senha, last_reg;
    EditText local, locallatlon;
    long tempo = Long.valueOf(5000);
    float distancia = 0;
    ArrayList<Double> latitude;
    ArrayList<Double> longitude;
    Double Lat, Lon;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_registro);
        local = (EditText) findViewById(R.id.ETlocal);
        locallatlon = (EditText) findViewById(R.id.ETlocalatlon);

        setTitle("NOVO REGISTRO");
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle extras = getIntent().getExtras();
        latitude = new ArrayList<Double>();
        longitude = new ArrayList<Double>();

        if (extras != null) {
            id = extras.getString("id");
            email = extras.getString("email");
            nome = extras.getString("nome");
            senha = extras.getString("senha");
            senha = extras.getString("senha");
            last_reg = extras.getString("id_regstro");

        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {//PEGA LATITUDE E LONGITUDE A CADA MUDANÇA
        locallatlon.setText(location.getLatitude() + "/" + location.getLongitude());
        Log.e(TAG, "onLocationChanged: ...");
        latitude.add(location.getLatitude());
        longitude.add(location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }

    public void btnIniciar(View view) {
        new NovoRegistro.HttpAsyncTaskSend().execute();//POST LOCAL E USUARIO

        locallatlon.setText("Buscando local ...");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permitir uso da localização nas configurações", Toast.LENGTH_SHORT).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, tempo, distancia, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, tempo, distancia, this);

    }


    public void btnfim(View view) {
        for(int x = 0; x < latitude.size(); x++){
            Lat = latitude.get(x);
            Lon = longitude.get(x);
            new HttpAsyncTaskSendGeo().execute();//POST GEOLOCAL
        }

    }


    class HttpAsyncTaskSend extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(NovoRegistro.this);
            dialog.show();
            dialog.setMessage("Creating...");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            Log.e(TAG, s);
        }

        @Override
        protected String doInBackground(String... strings) {//Local e usuario
            try {
                URL url = new URL("https://wessner.000webhostapp.com/registro_post.php");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                ContentValues values = new ContentValues();
                values.put("Local", local.getText().toString());
                values.put("Usuario_id", id);
                OutputStream out = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(getFormData(values));
                writer.flush();
                int status = urlConnection.getResponseCode();

                if (status == 200) {
                    InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder builder = new StringBuilder();

                    String inputString;
                    while ((inputString = bufferedReader.readLine()) != null) {
                        builder.append(inputString);
                    }
                    urlConnection.disconnect();
                    return builder.toString();
                }
            } catch (Exception ex) {
                return "Erro" + ex.getLocalizedMessage();
            }
            return null;
        }

        private String getFormData(ContentValues values) throws UnsupportedOperationException {
            try {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, Object> entry : values.valueSet()) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append("&");
                    }
                    sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    sb.append("=");
                    sb.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class HttpAsyncTaskSendGeo extends AsyncTask<String, Void, String> {//localizaçao
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(NovoRegistro.this);
            dialog.show();
            dialog.setMessage("Creating...");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            Log.e(TAG, s);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL("https://wessner.000webhostapp.com/geolocal_post.php");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                ContentValues values = new ContentValues();
                values.put("Lat",Lat);
                values.put("Lon", Lon);
                int registro = Integer.parseInt(last_reg);
                values.put("Registro_id", registro+1);//Ultimo registro inserido+ 1
                OutputStream out = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(getFormData(values));
                writer.flush();
                int status = urlConnection.getResponseCode();

                if (status == 200) {
                    InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder builder = new StringBuilder();

                    String inputString;
                    while ((inputString = bufferedReader.readLine()) != null) {
                        builder.append(inputString);
                    }
                    urlConnection.disconnect();
                    return builder.toString();
                }
            } catch (Exception ex) {
                return "Erro" + ex.getLocalizedMessage();
            }
            return null;
        }

        private String getFormData(ContentValues values) throws UnsupportedOperationException {
            try {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, Object> entry : values.valueSet()) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append("&");
                    }
                    sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    sb.append("=");
                    sb.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}