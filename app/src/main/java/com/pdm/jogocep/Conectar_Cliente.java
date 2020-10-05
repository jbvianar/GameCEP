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
import com.pdm.jogocep.model.Jogador;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Conectar_Cliente extends AppCompatActivity {

    TextView tvStatus, textStatusJogo2, tvNumPìngsPongs;
    Socket clientSocket;
    DataOutputStream socketOutput;
    BufferedReader socketEntrada;
    DataInputStream socketInput;
    private TextInputEditText IPS;
    Button btconecServer, btJoga;
    long pings, pongs;
    TextView cepfim;
    TextView cepinicio;
    String cepCli, cidadeCli, logradouroCli;
    TextView ipt;
    TextView tv, end2, cidade2, cep2;
    Jogador jogCliente;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conectar__cliente);

        tvStatus = findViewById(R.id.textStatus1);
        btconecServer = findViewById(R.id.btConectaServer);
        btJoga = findViewById(R.id.btJogar2);

        IPS = (TextInputEditText) findViewById(R.id.edtIPServer);

        cep2 = (TextView) findViewById(R.id.tvCep3);
        cidade2 = (TextView) findViewById(R.id.textCidade3);
        end2 = (TextView) findViewById(R.id.textEnd3);
        cepinicio = (TextView) findViewById(R.id.edtCepInicio2);
        cepfim = (TextView) findViewById(R.id.tvCepFim2);
        textStatusJogo2=(TextView)findViewById(R.id.textStatus2);

        //Recuperar os dados enviados
        Bundle dados = getIntent().getExtras();
        cepCli = dados.getString("CEP");
        cidadeCli = dados.getString("localidade");
        logradouroCli = dados.getString("logradouro");
        Log.v("PDM", "CEP: " + cepCli + ", Cidade: "+ cidadeCli + ", Logradouro: " + logradouroCli);

        // Configurar valores recuperados
        cidade2.setText(cidadeCli);
        end2.setText(logradouroCli);
        cep2.setText(cepCli);
        jogCliente = new Jogador();
        jogCliente.setCEPCliente(cepCli);

        Log.v("PDM " + "CEP Cliente", cepCli);

        btJoga.setEnabled(false);


    }

    public void atualizarStatus() {

        tvNumPìngsPongs.post(new Runnable() {
            @Override
            public void run() {
                tvNumPìngsPongs.setText("Enviados " + pings + " Pings e " + pongs + " Pongs");
            }
        });
    }

    public void onClickConectar (View v){
        final String ip = IPS.getText().toString();
        tvStatus.setText("Conectando em "+ ip);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket = new Socket(ip, 9090);
                    Log.v("PDM " , "Conectado "+ ip);

                    tvStatus.post(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Conectado com " + ip );
                            btJoga.setEnabled(true);
                            // btconecServer.setEnabled(false);
                        }
                    });
                    jogCliente.setPorta(9090);
                    socketOutput =
                            new DataOutputStream(clientSocket.getOutputStream()); //Envia dados
                    socketInput =
                            new DataInputStream(clientSocket.getInputStream());// Recebe dados

                    socketOutput.writeUTF(cepCli);
                    socketOutput.flush();
                    Log.v("PDM", "Enviou CEP "+ cepCli);

                    String CEPServidor = "";

                    if (CEPServidor.compareTo("") == 0) {
                        Log.v("PDM", "Antes de ler");
                        String result = socketInput.readUTF();
                        Log.v("PDM", "Result " + result);
                        if (result.compareTo("") != 0) {
                            CEPServidor = result;
                            Log.v("PDM", "CEPServidor " + CEPServidor);
                        }
                        if (CEPServidor.compareTo("") != 0) {
                            jogCliente.setCEPServer(CEPServidor);
                            final String finaldocep = CEPServidor.substring(3);
                            cepfim.post(new Runnable() {//<< só se pode mudar elementos da interface de uma thread diferente através do método post
                                @Override
                                public void run() {
                                    cepfim.setText(finaldocep);
                                    textStatusJogo2.setText("Digite os 3 digitos do CEP");

                                }
                            });
                        }

                    }



                    if (jogCliente.getCEPCliente() != null && jogCliente.getCEPServer() != null) {
                        Log.v("PDM", "Abrindo jogo");

                                    /*if (result.compareTo("PING") == 0) {
                                //enviar Pong
                                pongs++;
                                socketOutput.writeUTF("PONG");
                                socketOutput.flush();
                                atualizarStatus();
                            }*/
                    }
                    //}



                } catch (Exception e) {

                    tvStatus.post(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Erro na conexão com " + ip );
                            btJoga.setEnabled(false);
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
                        tvStatus.setText("Cliente Desconectado");
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


