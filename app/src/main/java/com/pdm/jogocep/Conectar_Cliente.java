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

    TextView tvStatus, tvNumPìngsPongs;
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

    //private Handler handler = new Handler();  //permite acesso da thred para UI onde tem o Handler

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
        cepfim = (TextView) findViewById(R.id.tvCepFim);

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
        jogCliente = new Jogador();
        jogCliente.setCEPCliente(cepCli);

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

    /*public void onClickConectar(View v) {
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
    }*/
           public void onClickConectar (View v){
            final String ip = IPS.getText().toString();
            tvStatus.setText("Conectando em "+ ip);
               //tvStatus.post(new Runnable() {
              //     @Override
               //    public void run() {
                //       tvStatus.setText("Conectando em " + ip );
               //    }
              // });
               //String CEPServidor = "";
               //String result = "";
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

                   while (socketInput != null) {
                       String result = socketInput.readUTF();
                       Log.v("PDM", "Result " + result);
                       String CEPServidor = "";
                       //if (jogCliente.getCEPServer() == null) {

                       if (result.compareTo("CEPServ") == 0) {
                           CEPServidor = result;
                           Log.v("PDM", "CEPServidor " + CEPServidor);
                           socketOutput.writeUTF("CEPCli");
                           socketOutput.flush();
                           atualizarStatus();
                       }

                       if (CEPServidor != null) {
                           jogCliente.setCEPServer(CEPServidor);
                           cepfim.setText(CEPServidor.substring(3));
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
                   }



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


