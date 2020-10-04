package com.pdm.jogocep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener {

    //private Spinner spn_nomes;

    private ArrayAdapter<String> adpNomes;

    int nome = 0; //Joao
    int cepMain;
    ImageView iv;
    String urlImage;
    private TextInputEditText CEP;
    Boolean CepValido;
    Button btServ,btCli;
    String ipAddress,cepcli, logradourocli,cidadecli;
    String cepserv, logradouroserv,cidadeserv;
    String cep, logradouro,cidade;
    TextView tv;
    private Handler handler = new Handler();  //permite acesso da thred para UI onde tem o Handler


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//Setando o Layout a ser carregado

        // acessando os componentes
        iv = (ImageView)findViewById(R.id.imageView);
        CEP = (TextInputEditText)findViewById(R.id.edtCep);
        tv = (TextView)findViewById(R.id.textCidade2);
        btServ= (Button)findViewById(R.id.btServer);
        btCli = (Button)findViewById(R.id.btClient);


        CepValido = false;
        btServ.setEnabled(false);
        btCli.setEnabled(false);

        //Criando máscara para o CEP
        SimpleMaskFormatter smf = new SimpleMaskFormatter("NNNNN-NNN");
        MaskTextWatcher mtw = new MaskTextWatcher(CEP,smf);
        CEP.addTextChangedListener(mtw);


        //Carregando o Spinner
        Spinner spn_nomes = (Spinner) findViewById(R.id.spnNomes);
        // Definindo o Layout pro Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spnNomes, R.layout.layout_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Definindo o Adapter do Spinner
        spn_nomes.setAdapter(adapter);
        spn_nomes.setOnItemSelectedListener(this);



        /*spn_nomes = (Spinner)findViewById(R.id.spnNomes);
        adpNomes = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
        adpNomes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_nomes.setAdapter(adpNomes);
        adpNomes.add("Miguel");
        adpNomes.add("João"); */



    }



    public void validaCep(View view){

        ConnectivityManager connManager;
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connManager.getAllNetworks();
        for(Network minhaRede:networks){
            NetworkInfo netInfo = connManager.getNetworkInfo(minhaRede);

            if(netInfo.getState().equals(NetworkInfo.State.CONNECTED)){

                NetworkCapabilities propRede = connManager.getNetworkCapabilities(minhaRede);

                if(propRede.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    int ip =wifiManager.getConnectionInfo().getIpAddress();
                    ipAddress = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

                }
            }
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                testeDeHttp();
            }
        });
        t.start();
    }

    private void testeDeHttp() {
        try {
            cep = CEP.getText().toString();
            URL url = new URL("https://viacep.com.br/ws/"+cep+"/json/");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            int resposta = conn.getResponseCode();

            if(resposta == HttpsURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
                StringBuilder response = new StringBuilder();
                String line =null;
                while ((line = br.readLine())!=null){
                    response.append(line.trim());

                }
                JSONObject resultado = new JSONObject(response.toString());
                logradouro = resultado.getString("logradouro");
                cidade = resultado.getString("localidade");

                Log.v("PDM", "Localidade: "+cidade);
                /*t.post(new Runnable() {
                    @Override
                    public void run() {
                        t.setText("Seu ip para criar servidor é esse: "+ipAddress+" :9090");
                    }
                });*/
                CepValido = true;

                handler.post(new Runnable() {// semelhante a runOnUiThready
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Inicie o Jogo como Servidor ou Cliente",Toast.LENGTH_LONG).show();
                        tv.setText("Cidade:"+ cidade);
                    }
                });

                //Habilitar  Botões de iniciar
                btServ.post(new Runnable() {
                    @Override
                    public void run() {
                        btServ.setEnabled(true);
                    }
                });
                btCli.post(new Runnable() {
                    @Override
                    public void run() {
                        btCli.setEnabled(true);
                    }
                });


            }else{
               /* t.post(new Runnable() {
                    @Override
                    public void run() {
                        t.setText("Cep invalido");
                    }
                });*/

                Log.v("PDM", "CEP inválido");
                CepValido = false;

                handler.post(new Runnable() {// semelhante a runOnUiThready
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Digite um CEP valido e verifique",Toast.LENGTH_SHORT).show();
                        tv.setText("");
                    }
                });

                //Desabilitar  Botões de iniciar
                btServ.post(new Runnable() {
                    @Override
                    public void run() {
                        btServ.setEnabled(false);
                    }
                });
                btCli.post(new Runnable() {
                    @Override
                    public void run() {
                        btCli.setEnabled(false);
                    }
                });

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onClickServer(View v){

        Log.v("PDM", "CEP Server "+ cep);
        Log.v("PDM", "logradouro "+ logradouro);
        Log.v("PDM", "localidad "+ cidade);

        Intent intent = new Intent(getApplicationContext(),ConectarServidor.class);

        //passar dados

        intent.putExtra("CEP",cep);
        intent.putExtra("logradouro",logradouro);
        intent.putExtra("localidade",cidade);


        startActivity(intent);
    }
    public void onClickClient(View v){

        Intent intent = new Intent(getApplicationContext(),Conectar_Cliente.class);
        //passar dados
        intent.putExtra("CEP",cep);
        intent.putExtra("logradouro",logradouro);
        intent.putExtra("localidade",cidade);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {

        switch (nome) {

            case 0://vazio
                urlImage="";
                break;
            case 1: //Eloy

                urlImage = "https://i.pinimg.com/originals/f6/6e/26/f66e267bd5b10b07b2436a88010ccb5e.jpg";
                break;
            case 2: //Morlock
                urlImage = "https://i.pinimg.com/originals/3e/5a/98/3e5a986afeeb5e04834fe7e6bab61ee6.png";
                break;

        }



    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        nome = i;
        switch (nome) {

            case 0://vazio
                urlImage="";
                break;
            case 1: //Eloy

                urlImage = "https://i.pinimg.com/originals/3e/5a/98/3e5a986afeeb5e04834fe7e6bab61ee6.png";
                break;
            case 2: //Morlock

                urlImage = "https://i.pinimg.com/originals/f6/6e/26/f66e267bd5b10b07b2436a88010ccb5e.jpg";
                break;

        }

        Log.v("PDM", "BAIXANDO IMAGEM E COLANDO NA VIEW");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap b = loadImageFromNetwork(urlImage);

                //Log.v("PDM", "Imagem baixada com " + b.getByteCount() + " bytes");

                try {
                    iv.post(new Runnable() {
                        @Override
                        public void run() {
                            iv.setImageBitmap(b);
                        }
                    });

                } catch (Exception e) {
                    Log.v("PDM", "Não é possível acessar a Thread UI");
                    e.printStackTrace();
                }
            }
        }
        );
        t.start();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    private Bitmap loadImageFromNetwork(String url) {

        try {
            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void listConnections() {
        ConnectivityManager connManager;
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connManager.getAllNetworks();
        NetworkInfo networkInfo;


        for (Network mNetwork : networks) {
            networkInfo = connManager.getNetworkInfo(mNetwork);
            if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {

                Log.v("PDM", "tipo de conexão conectada:" + networkInfo.getTypeName());

            }
        }
    }



}