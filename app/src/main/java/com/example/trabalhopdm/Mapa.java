package com.example.trabalhopdm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

public class Mapa extends AppCompatActivity implements LocationListener {
    private String urlBase = "http://maps.googleapis.com/maps/api" +
            "/staticmap?size=400x400&sensor=true" +
            "&markers=color:red|-23.262626262,-24.562259494";
    private WebView mapa;
    String id, idreg, data, local;
    List Latitude, Longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        mapa = findViewById(R.id.mapa);
        TextView tv = findViewById(R.id.text);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            id = extras.getString("id");
            idreg = extras.getString("id_reg");
            data = extras.getString("data");
            local = extras.getString("local");
        }
        tv.setText(id+idreg+local+data);
        new Mapa.HttpAsyncTaskSend().execute();//get latitude e longitude do local
        Latitude = new ArrayList<Double>();
        Longitude = new ArrayList<Double>();

        /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:-19.8516098,-43.9509601?z=15"));
        startActivity(intent);*/
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        String url = String.format(urlBase,-23.656562626,-24.5656254/8);
        mapa.loadUrl(url);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    class HttpAsyncTaskSend extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Mapa.this);
            dialog.show();
            dialog.setMessage("Creating...");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            Log.e(TAG, ">>>>>>>>>>>>>>>  "+s);
            try {
                loadData(s);
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {//Local e usuario
            try {
                URL url = new URL("https://wessner.000webhostapp.com/post_lista.php");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                ContentValues values = new ContentValues();
                values.put("idreg", idreg);
                values.put("idper",id);
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
    private void loadData(String data) throws JSONException {
        boolean flag = false;
        JSONObject res = new JSONObject(data);
        JSONArray array = res.getJSONArray("GeoLoc");
        Log.e(TAG, "loadData ");

        for (int i =0;i < array.length(); i++){
            JSONObject json = array.getJSONObject(i);
            String Lat = json.get("Lat").toString();
            String Lon = json.get("Lon").toString();
            Latitude.add(Double.parseDouble(Lat));
            Longitude.add(Double.parseDouble(Lon));
            Log.e(TAG, "loadData "+id+" > "+idreg +" > "+Lat + "   > " + Lon);
        }
    }
}