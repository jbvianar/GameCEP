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
import android.widget.EditText;
import android.widget.TextView;

import com.pdm.jogocep.model.Jogador;

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

    TextView tvStatus, textPointS, textTentS,textStatusJogo1,tvNumPìngsPongs;
    ServerSocket welcomeSocket;
    DataOutputStream socketOutput;
    BufferedReader socketEntrada;
    DataInputStream fromClient;
    boolean continuarRodando = false;
    Button btLigarServer,btJoga;
    TextView getTextPointS, getTextTentS;
    Jogador jogServidor;
    String cepserv,cidadeserv,logradouroserv,cepcli,cepend;
    int numInsert,numReal;
    int pts=0;
    int tents=0;
    TextView ipt;
    TextView tv;
    TextView end;
    TextView cep2;
    EditText cepini;
    TextView cepfim;
    //private Handler handler = new Handler();  //permite acesso da thred para UI onde tem o Handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conectar_servidor);

        tvStatus = findViewById(R.id.textStatus);
        btLigarServer = findViewById(R.id.btConectaServer);
        btJoga = findViewById(R.id.btJogar1);
        cep2 = (TextView)findViewById(R.id.tvCep2);
        ipt = (TextView)findViewById(R.id.textIP);
        tv = (TextView)findViewById(R.id.textCidade2);
        end = (TextView)findViewById(R.id.textEnd);
        cepini=(EditText)findViewById(R.id.edtCepInicio);
        cepfim=(TextView)findViewById(R.id.tvCepFim);
        textStatusJogo1=(TextView)findViewById(R.id.textStatus1);
        textPointS=(TextView)findViewById(R.id.textPont1);
        textTentS=(TextView)findViewById(R.id.textTenta1);

        //Recuperar os dados enviados
        Bundle dados = getIntent().getExtras();
        cepserv = dados.getString("CEP");
        cidadeserv = dados.getString("localidade");
        logradouroserv = dados.getString("logradouro");
        Log.v("PDM", "CEP: " + cepserv + ", Cidade: "+ cidadeserv + ", Logradouro: " + logradouroserv);
        //Configurar valores recuperados
        tv.setText(cidadeserv);
        end.setText(logradouroserv);
        cep2.setText(cepserv);
        jogServidor = new Jogador();
        jogServidor.setCEPServer(cepserv);
        cepcli=jogServidor.getCEPServer().substring(0,3);

       numReal = Integer.parseInt(cepcli);
        Log.v("PDM ","CEP Server"+jogServidor.getCEPServer());
        Log.v("PDM ","CEP Server"+ cepserv);

        btJoga.setEnabled(false);



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



    public void ligarServerCodigo () {
        //Desabilitar o Botão de Ligar
        btLigarServer.post(new Runnable() {
            @Override
            public void run() {
                btLigarServer.setEnabled(false);


            }
        });
        String CEPCliente = "";
        String result = "";
        try {
            Log.v("PDM", "Ligando o Server");
            welcomeSocket = new ServerSocket(9090);
            Socket connectionSocket = welcomeSocket.accept();
            Log.v("PDM", "Nova conexão");
            //atualizarStatus();


            //Instanciando os canais de stream
            fromClient = new DataInputStream(connectionSocket.getInputStream());
            socketOutput = new DataOutputStream(connectionSocket.getOutputStream());
            continuarRodando = true;

            socketOutput.writeUTF(cepserv);
            socketOutput.flush();
            Log.v("PDM", "Enviou CEP "+ cepserv);

            if (CEPCliente.compareTo("") == 0) {
                Log.v("PDM", "Antes de ler");
                result = fromClient.readUTF();
                jogServidor.setCEPCliente(result);
                Log.v("PDM", "readUTF Result = " + result);
                if (result.compareTo("") != 0) {
                    CEPCliente = result;
                }
                if (CEPCliente.compareTo("") != 0) {
                    jogServidor.setCEPCliente(CEPCliente);
                    final String finaldocep = CEPCliente.substring(3);
                    cepfim.post(new Runnable() {
                        @Override
                        public void run() {
                            cepfim.setText(finaldocep);
                            cepend=finaldocep;
                           textStatusJogo1.setText("Digite os 3 digitos do CEP");
                            if (cepini.getText().toString() != ""){
                               btJoga.setEnabled(true);
                           }
                        }
                    });
                }
            }


            Log.v("PDM", "readUTF Result = " + result);


            Log.v("PDM", result);
            //Enviando dados para o servidor
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


   // public void atualizarStatus () {

      //  tvNumPìngsPongs.post(new Runnable() {
     //       @Override
      //      public void run() {
                // tvNumPìngsPongs.setText("Enviados " + pings + " Pings e " + pongs + " Pongs");
       //     }
     //   });
   //}

    public void onClickJogar(View v){

        String num = cepini.getText().toString();//número inserido  String
        Log.v("PDM", "aqui0.5");
        numInsert = Integer.parseInt(num);//número inserido em forma de int
        //numReal = Integer.parseInt((jogServidor.getCEPCliente()));
        Log.v("PDM", "NumReal "+jogServidor.getCEPServer() + "inserido " + numInsert);

        if (numReal == numInsert){

            textStatusJogo1.setText("ACERTOU!");

            tents++;
            textTentS.setText(String.valueOf(tents));

        } else {
            tents++;
            pts = pts + (1000-Math.abs(numInsert-numReal));
            textPointS.setText(String.valueOf(pts));
            Log.v("PDM", "aqui2.3");
            textTentS.setText(String.valueOf(tents));
            Log.v("PDM", "aqui2.4");
            if (numReal > numInsert) {

                textStatusJogo1.setText("MAIOR");
            } else {
                Log.v("PDM", "aqui4");
                textStatusJogo1.setText("MENOR");
            }
        }


    }


}
