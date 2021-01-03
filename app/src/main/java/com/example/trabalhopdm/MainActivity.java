package com.example.trabalhopdm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;
import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class MainActivity extends Activity{
    List<Map<String,String>> lista;
    private EditText email, senha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = findViewById(R.id.ETemail);
        senha = findViewById(R.id.ETsenha);

    }

    class HttpAsyncTask extends AsyncTask<String, Void, String>{
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.show();
            dialog.setMessage("Getting data...");
        }


        @Override
        protected String doInBackground(String... strings) {
            try{
                URL url = new URL("https://wessner.000webhostapp.com/banco_select_pessoa.php");
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

    public void BTNcadastrar(View view) {
        Intent it = new Intent(this, Cadastro.class);
        startActivity(it);
    }

    public void BTNsignin(View view) {
        String em = email.getText().toString();
        //HttpAsyncTask task = new HttpAsyncTask();
        //task.execute();
        new HttpAsyncTask().execute(em);
    }

    private void loadData(String data) throws JSONException {
        boolean flag = false;
        JSONObject res = new JSONObject(data);
        JSONArray array = res.getJSONArray("Pessoas");

        for (int i =0;i < array.length(); i++) {
              JSONObject json = array.getJSONObject(i);
              String mail = json.get("Email").toString();
              String sen = json.get("Pass").toString();
              String nom = json.get("Name").toString();
              String id = json.get("ID").toString();
              Log.e(TAG, "aqi"+i);
              //BUSCA TODAS AS PESSOAS E VERIFICA EQUIVALENCIA DE ENTRA NO LOOP
              if (mail.equals(email.getText().toString()) && sen.equals(senha.getText().toString())){
                  flag = true;
                  Intent intent = new Intent(this, Registro.class);//PASSA INFORMAÇÕES PARA A ACTIVITY 'REGISTRO'
                  intent.putExtra("id",id);
                  intent.putExtra("email", mail);
                  intent.putExtra("nome",nom);
                  intent.putExtra("senha", sen);
                  startActivity(intent);
              }
          }
        if (flag == false) {
            Toast.makeText(getApplicationContext(), "EMAIL OU SENHA INVÁLIDA!", Toast.LENGTH_SHORT).show();
        }
    }


}