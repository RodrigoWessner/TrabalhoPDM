package com.example.trabalhopdm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

public class Registro extends AppCompatActivity {
    String id, email, nome, senha;
    EditText local;
    ListView listView;
    List<Map<String, String>> lista;
    String de [] = {"D","L","I"};
    String lastReg;
    String id_reg;
    int[] para = {R.id.EData, R.id.ELocal, R.id.EId};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        listView = findViewById(R.id.listview);

        lista = new ArrayList<>();

        new Registro.HttpAsyncTask().execute();//BUSCA REGISTROS PARA A LISTVIEW

        Bundle extras = getIntent().getExtras();
        if(extras != null){//PEGA INFORMAÇOES DO LOGIN/MAINACTIVITY
            id = extras.getString("id");
            email = extras.getString("email");
            nome = extras.getString("nome");
            senha = extras.getString("senha");
        }
        setTitle("Olá, "+ nome);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "onItemClick: position "+ i);
                TextView Eid = view.findViewById(R.id.EId);
                TextView Edata = view.findViewById(R.id.EData);
                TextView Elocal = view.findViewById(R.id.ELocal);
                id_reg = Eid.getText().toString();
                Intent it = new Intent(getApplicationContext(), Mapa.class);//PASSA INFORMAÇOES DO REGISTRO E PESSOA PARA A ACTVITY MAPA
                it.putExtra("id", id);
                it.putExtra("id_reg", id_reg);
                it.putExtra("data", Edata.getText().toString());
                it.putExtra("local", Elocal.getText().toString());
                startActivity(it);
            }
        });
    }

    class HttpAsyncTask extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Registro.this);
            dialog.show();
            dialog.setMessage("Getting data...");
        }


        @Override
        protected String doInBackground(String... strings) {
            try{
                URL url = new URL("https://wessner.000webhostapp.com/select_registro.php");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                int status = urlConnection.getResponseCode();

                if (status == 200){
                    InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder builder = new StringBuilder();

                    String inputString;
                    while((inputString = bufferedReader.readLine()) != null){
                        builder.append(inputString);
                    }
                    urlConnection.disconnect();
                    return builder.toString();
                }
            }catch (Exception ex){
                Log.e("URL", ex.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            dialog.setMessage("Wait...");
            Log.e(TAG, "onPostExecute: " + s);
            try {
                loadData(s);
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
    private void loadData(String data) throws JSONException {
        boolean flag = false;
        JSONObject res = new JSONObject(data);
        JSONArray array = res.getJSONArray("Registro");
        Log.e(TAG, "loadData ");

        for (int i =0;i < array.length(); i++){
            JSONObject json = array.getJSONObject(i);
            String usuarioId = json.get("Usuario_id").toString();
            Log.e(TAG, "loadData "+ usuarioId + "   > " + id);
            if(usuarioId.equals(id)){
                String local = json.get("Local").toString();
                String dat = json.get("Data").toString();
                lastReg = json.get("IdRegistro").toString();//Ultimo registro do usuario (for/ usuario id = id)

                Map<String, String> mapa = new HashMap<>();//Mapa Listagem Local e Data
                mapa.put("D", dat);
                mapa.put("L", local);
                mapa.put("I", lastReg);
                lista.add(mapa);
            }
            SimpleAdapter adapter = new SimpleAdapter(this, lista, R.layout.listagem, de, para);
            listView.setAdapter(adapter);
        }
    }

    public void BTNregistro(View view) {
        Intent intent = new Intent(this, NovoRegistro.class);//PASSA INFORMAÇOES PARA A PROXIMA ACTIVTY/NOVOREGISTRO
        intent.putExtra("id",id);
        intent.putExtra("email", email);
        intent.putExtra("nome",nome);
        intent.putExtra("senha", senha);
        intent.putExtra("id_regstro", lastReg);
        startActivity(intent);
    }
}