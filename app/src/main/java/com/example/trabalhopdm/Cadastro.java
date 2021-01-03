package com.example.trabalhopdm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.DialogTitle;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.icu.text.CaseMap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

public class Cadastro extends AppCompatActivity {
    EditText nome, email, senha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        setTitle("CADASTRO");
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
       /* getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(Color.parseColor("#696969")));
        */
        nome = findViewById(R.id.ETnome);
        email = findViewById(R.id.ETemailcad);
        senha = findViewById(R.id.ETsenhacad);

    }


    public void BTNcadastro(View view) {
        /*HttpAsyncTaskSend taskSend = new HttpAsyncTaskSend();
        taskSend.execute("https://wessner.000webhostapp.com/banco_post_pessoa.php");//service
    */
        new Cadastro.HttpAsyncTaskSend().execute();
    }

    class HttpAsyncTaskSend extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Cadastro.this);
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
            try{
                URL url = new URL("https://wessner.000webhostapp.com/banco_post_pessoa.php");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                ContentValues values = new ContentValues();
                values.put("Name", nome.getText().toString());
                values.put("Email", email.getText().toString());
                values.put("Pass", senha.getText().toString());
                OutputStream out = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(getFormData(values));
                writer.flush();
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
                return "Erro"+ ex.getLocalizedMessage();
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