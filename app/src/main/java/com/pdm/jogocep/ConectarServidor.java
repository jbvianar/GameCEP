package com.pdm.jogocep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ConectarServidor extends AppCompatActivity {

    TextView tvStatus, tvNumPìngsPongs;
    ServerSocket welcomeSocket;
    DataOutputStream socketOutput;
    BufferedReader socketEntrada;
    DataInputStream fromClient;
    boolean continuarRodando = false;
    private TextInputEditText CEP1;
    Button btLigarServer;
    long pings, pongs;
    String cepMaster;
    TextView ipt;
    Boolean CepValido;
    Button btServ,btCli;
    String ipAddress,cep, logradouro,cidade;
    TextView tv,end;
    private Handler handler = new Handler();  //permite acesso da thred para UI onde tem o Handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conectar_servidor);


        tvStatus = findViewById(R.id.textStatus);
        btLigarServer = findViewById(R.id.btLigarServer);
        CEP1 = (TextInputEditText)findViewById(R.id.edtCepServ);
        ipt = (TextView)findViewById(R.id.textIP);
        tv = (TextView)findViewById(R.id.textCidade2);
        end = (TextView)findViewById(R.id.textEnd);


        //Criando máscara para o CEP
        SimpleMaskFormatter smf = new SimpleMaskFormatter("NNNNN-NNN");
        MaskTextWatcher mtw = new MaskTextWatcher(CEP1,smf);
        CEP1.addTextChangedListener(mtw);

        //Recuperar os dados enviados
        Bundle dados = getIntent().getExtras();
        cepMaster = dados.getString("CEP");
        //Configurar valores recuperados
        //porta.setText(cepmaster);
        Log.v("PDM "+"CEP Master",cepMaster);


    }

    public void ligarServidor(View v) {
        ConnectivityManager connManager;
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        Network[] networks = connManager.getAllNetworks();


        for (Network minhaRede : networks) {
            NetworkInfo netInfo = connManager.getNetworkInfo(minhaRede);
            if (netInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                NetworkCapabilities propDaRede = connManager.getNetworkCapabilities(minhaRede);

                if (propDaRede.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

                    String macAddress = wifiManager.getConnectionInfo().getMacAddress();
                    Log.v("PDM", "Wifi - MAC:" + macAddress);

                    int ip = wifiManager.getConnectionInfo().getIpAddress();
                    String ipAddress = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

                    Log.v("PDM", "Wifi - IP:" + ipAddress);
                    tvStatus.setText("Ativo");
                    ipt.setText(ipAddress);

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ligarServerCodigo();
                        }
                    });
                    t.start();
                }

            }

        }
    }


    public void mandarPing(View v) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socketOutput != null) {
                        socketOutput.writeUTF("PING");
                        socketOutput.flush();
                        pings++;
                        atualizarStatus();
                    } else {
                        tvStatus.setText("Cliente Desconectado");
                        btLigarServer.setEnabled(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

    }

        public void desconectar (View view) {
            try {
                if (socketOutput != null) {
                    socketOutput.close();
                }
                //Habilitar o Botão de Ligar
                btLigarServer.post(new Runnable() {
                    @Override
                    public void run() {
                        btLigarServer.setEnabled(true);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void ligarServerCodigo () {
            //Desabilitar o Botão de Ligar
            btLigarServer.post(new Runnable() {
                @Override
                public void run() {
                    btLigarServer.setEnabled(false);
                }
            });

            String result = "";
            try {
                Log.v("SMD", "Ligando o Server");
                welcomeSocket = new ServerSocket(9090);
                Socket connectionSocket = welcomeSocket.accept();
                Log.v("SMD", "Nova conexão");

                //Instanciando os canais de stream
                fromClient = new DataInputStream(connectionSocket.getInputStream());
                socketOutput = new DataOutputStream(connectionSocket.getOutputStream());
                continuarRodando = true;
                while (continuarRodando) {
                    result = fromClient.readUTF();
                    if (result.compareTo("PING") == 0) {
                        //enviar Pong
                        pongs++;
                        socketOutput.writeUTF("PONG");
                        socketOutput.flush();
                        atualizarStatus();
                    }
                }

                Log.v("SMD", result);
                //Enviando dados para o servidor
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void somarNumPongs () {
            pongs++;
            atualizarStatus();

        }

        public void atualizarStatus () {
            //Método que vai atualizar os pings e pongs, usando post para evitar problemas com as threads
            tvNumPìngsPongs.post(new Runnable() {
                @Override
                public void run() {
                    tvNumPìngsPongs.setText("Enviados " + pings + " Pings e " + pongs + " Pongs");
                }
            });
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
            cep = CEP1.getText().toString();
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
                        Toast.makeText(getApplicationContext(),"CEP do Oponente Válido",Toast.LENGTH_LONG).show();
                        tv.setText(cidade);
                        end.setText(logradouro);
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


    }
