package com.pdm.jogocep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Conectar_Cliente extends AppCompatActivity {

    TextView tvStatusIP, tvNumPìngsPongs;
    Socket clientSocket;
    DataOutputStream socketOutput;
    BufferedReader socketEntrada;
    DataInputStream socketInput;
    private TextInputEditText IPS;
    Button btconecServer, btJoga;
    long pings, pongs;
    String cepMaster;
    String cepCli, cidadeCli, logradouroCli;
    TextView ipt;
    TextView tv, end2, cidade2, cep2;
    //private Handler handler = new Handler();  //permite acesso da thred para UI onde tem o Handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conectar__cliente);

        tvStatusIP = findViewById(R.id.textStatus1);
        btconecServer = findViewById(R.id.btConectaServer);
        btJoga = findViewById(R.id.btJogar2);

        IPS = (TextInputEditText) findViewById(R.id.edtIPServer);
        ipt = (TextView) findViewById(R.id.textIP);
        cep2 = (TextView) findViewById(R.id.tvCep3);
        cidade2 = (TextView) findViewById(R.id.textCidade3);
        end2 = (TextView) findViewById(R.id.textEnd3);

        //Criando máscara para o IP
       // SimpleMaskFormatter smf2 = new SimpleMaskFormatter("NNN.NNN.N.NN");
       // MaskTextWatcher mtw2 = new MaskTextWatcher(IPS, smf2);
       // IPS.addTextChangedListener(mtw2);

        //Recuperar os dados enviados
        Bundle dados = getIntent().getExtras();
        cepCli = dados.getString("CEP");
        cidadeCli = dados.getString("localidade");
        logradouroCli = dados.getString("logradouro");

      // Configurar valores recuperados
       cidade2.setText(cidadeCli);
       end2.setText(logradouroCli);
       cep2.setText(cepCli );

        Log.v("PDM " + "CEP Cliente", cepCli);

        btJoga.setEnabled(false);


    }

    public void atualizarStatus() {
        //Método que vai atualizar os pings e pongs, usando post para evitar problemas com as threads
        tvNumPìngsPongs.post(new Runnable() {
            @Override
            public void run() {
             tvNumPìngsPongs.setText("Enviados " + pings + " Pings e " + pongs + " Pongs");
            }
        });
    }

    public void onClickConectar(View v) {
        ConnectivityManager connManager;
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connManager.getAllNetworks();
        for (Network minhaRede : networks) {
            NetworkInfo netInfo = connManager.getNetworkInfo(minhaRede);
            if (netInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                NetworkCapabilities propDaRede = connManager.getNetworkCapabilities(minhaRede);
                if (propDaRede.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            conectarCodigo();
                        }
                    });
                    t.start();
                }
            }
        }
    }
           public void conectarCodigo (){
            final String ip = IPS.getText().toString();
            tvStatusIP.setText("Conectando em " + ip + ":9090");


            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        clientSocket = new Socket(ip, 9090);

                        tvStatusIP.post(new Runnable() {
                            @Override
                            public void run() {
                                tvStatusIP.setText("Conectado com " + ip + ":9090");
                            }
                        });
                        socketOutput =
                                new DataOutputStream(clientSocket.getOutputStream());
                        socketInput =
                                new DataInputStream(clientSocket.getInputStream());
                        while (socketInput != null) {
                            String result = socketInput.readUTF();
                            if (result.compareTo("PING") == 0) {
                                //enviar Pong
                                pongs++;
                                socketOutput.writeUTF("PONG");
                                socketOutput.flush();
                                atualizarStatus();
                            }
                        }


                    } catch (Exception e) {

                        tvStatusIP.post(new Runnable() {
                            @Override
                            public void run() {
                                tvStatusIP.setText("Erro na conexão com " + ip + ":9090");
                            }
                        });

                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }

        public void mandarPing (View v) {
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
                            tvStatusIP.setText("Cliente Desconectado");
                            btconecServer.setEnabled(true);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();

        }
            public void somarNumPongs () {
                pongs++;
                atualizarStatus();


            }

    }

