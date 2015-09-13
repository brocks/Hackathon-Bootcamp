package com.hacks.ram.ramhacks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class TwitterActivity extends AppCompatActivity {
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);

//      Populate Spinner
        populateSpinner();
//        Reload Button
        Button reloadBut= (Button) findViewById(R.id.reload);
        reloadBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TweetsTask().execute("MLHacks");
            }
        });

//        ListView
        listView = (ListView) findViewById(R.id.listView);

//        Get filters intent
        final String username = getIntent().getStringExtra("TWITTER_USERNAME");
//  Test the whole system
        new TweetsTask().execute("MLHacks");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_twitter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    Populate The List View
    void populateListView(ArrayList<Tweet> tweets){
        TweetAdapter  tweetAdapter = new TweetAdapter(this, R.layout.tweet_adapter, tweets);
        listView.setAdapter(tweetAdapter);
    }

    void populateSpinner(){
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private class TweetsTask extends AsyncTask<String, Void, ArrayList<Tweet>> {

        private ProgressDialog progressDialog;
        private static final String URL_BASE = "https://api.twitter.com";
        private static final String URL_BUSCA = URL_BASE + "/1.1/statuses/user_timeline.json?screen_name=";
        private static final String URL_AUTH = URL_BASE + "/oauth2/token";

        // Registre sua app no endereço https://dev.twitter.com/apps/new e obtenha as chaves de consumo.
        private static final String CONSUMER_KEY = "LkmthzJ97KUAXe5EnEEfZLAgK";
        private static final String CONSUMER_SECRET = "HL6I5vHcRJl9KHsfaikvv1P2HFgxFtkW5RZx6VEhcPCM54sZkI";


        private String autenticarApp(){

            HttpURLConnection conexao = null;
            OutputStream os = null;
            BufferedReader br = null;
            StringBuilder resposta = null;

            try {
                URL url = new URL(URL_AUTH);
                conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("POST");
                conexao.setDoOutput(true);
                conexao.setDoInput(true);

                // codificando as chaves de consumo da api com a codificação base64.
                String credenciaisAcesso = CONSUMER_KEY + ":" + CONSUMER_SECRET;
                String autorizacao = "Basic " + Base64.encodeToString(credenciaisAcesso.getBytes(), Base64.NO_WRAP);
                String parametro = "grant_type=client_credentials";

                // enviando as credenciais de acesso no cabeçalho da requisição
                conexao.addRequestProperty("Authorization", autorizacao);
                conexao.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                conexao.connect();

                // enviando o parametro granty_type no corpo da requisição
                os = conexao.getOutputStream();
                os.write(parametro.getBytes());
                os.flush();
                os.close();

                // recuperando a resposta do servidor (token de acesso  em JSON - obrigatório para utilizar a api)
                br = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
                String linha;
                resposta = new StringBuilder();

                while ((linha = br.readLine()) != null){
                    resposta.append(linha);
                }

                Log.d("Response Code", String.valueOf(conexao.getResponseCode()));
                Log.d("Response Json", resposta.toString());

            } catch (Exception e) {
                Log.e("Error POST", Log.getStackTraceString(e));

            }finally{
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
            return resposta.toString();
        }

        /**
         * Exibe um ProgressDialog na UI thread antes do processamento doInBacground.
         */
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = new ProgressDialog(TwitterActivity.this);
            progressDialog.setMessage("Loading Tweets");
            progressDialog.show();
        }

        /**
         * Busca os tweets mais recentes da timeline de um usuário. Método roda em segundo plano.
         *
         * @param param  O termo a buscar no twitter - nesse caso, o usuário
         * @return
         * <li>uma lista com os últimos tweets retirados da timeline do usuário.</li>
         */
        @Override
        protected ArrayList<Tweet> doInBackground(String... param) {

            String termoDeBusca = param[0];
            ArrayList<Tweet> tweets = new ArrayList<Tweet>();
            HttpURLConnection conexao = null;
            BufferedReader br = null;

            try {
                URL url = new URL(URL_BUSCA + termoDeBusca);
                conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("GET");

                // utilizando o token de acesso (formato JSON)
                String jsonString = autenticarApp();
                JSONObject jsonAcesso = new JSONObject(jsonString);
                String tokenPortador = jsonAcesso.getString("token_type") + " " +
                        jsonAcesso.getString("access_token");

                conexao.setRequestProperty("Authorization", tokenPortador);
                conexao.setRequestProperty("Content-Type", "application/json");
                conexao.connect();

                // recuperando os tweets da api
                br = new BufferedReader(new InputStreamReader(conexao.getInputStream()));

                String linha;
                StringBuilder resposta = new StringBuilder();

                while ((linha = br.readLine()) != null){
                    resposta.append(linha);
                }

                Log.d("Código resposta GET", String.valueOf(conexao.getResponseCode()));
                Log.d("Resposta JSON", resposta.toString());

                JSONArray jsonArray = new JSONArray(resposta.toString());
                JSONObject jsonObject;

                for (int i = 0; i < jsonArray.length(); i++) {

                    jsonObject = (JSONObject) jsonArray.get(i);
                    Tweet tweet = new Tweet();

                    tweet.setNome(jsonObject.getJSONObject("user").getString("name"));
                    tweet.setUsuario(jsonObject.getJSONObject("user").getString("screen_name"));
                    tweet.setUrlImagemPerfil(jsonObject.getJSONObject("user").getString("profile_image_url"));
                    tweet.setMensagem(jsonObject.getString("text"));
                    tweet.setData(jsonObject.getString("created_at"));

                    tweets.add(i, tweet);
                }

            } catch (Exception e) {
                Log.e("Erro GET: ", Log.getStackTraceString(e));

            }finally {
                if(conexao != null){
                    conexao.disconnect();
                }
            }
            return tweets;
        }

        /**
         * Atualiza a listview (na UI Thread) com os a lista de tweets recuperada pelo método
         * doInBackground. Mostra uma mensagem de erro caso a lista esteja vazia.
         *
         * @param tweets
         * a lista retornada pelo processamento doInBackground.
         */
        @Override
        protected void onPostExecute(ArrayList<Tweet> tweets){
            progressDialog.dismiss();

            if (tweets.isEmpty()) {
                Toast.makeText(TwitterActivity.this, "No Tweet",
                        Toast.LENGTH_SHORT).show();
            } else {
                populateListView(tweets);
                Toast.makeText(TwitterActivity.this, "Tweets",
                        Toast.LENGTH_SHORT).show();
            }
        }


    }
}
