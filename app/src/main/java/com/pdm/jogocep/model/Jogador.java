package com.pdm.jogocep.model;

import android.widget.TextView;

import java.io.Serializable;

public class Jogador implements Serializable {

    private String IP;// IP do server
    private int porta;//porta do server
    private String CEPServer;//onde está o jogador como Server
    private String CEPCliente;//CEP do jogador como Cliente
    private int nPtosServer;//número de pontos do jogador Server
    private int nPtosCliente;//número de pontos do jogador Cliente
    private int nTentativas;//número de tentativas do jogador
    private boolean isFimServer;//se o jogador Server já acertou o CEP
    private boolean isFimCliente;//se o jogador Cliente já acertou o CEP
    private boolean isWinner;//se o jogador venceu

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public String getCEPServer() {
        return CEPServer;
    }

    public void setCEPServer(String CEPServer) {
        this.CEPServer = CEPServer;
    }

    public String getCEPCliente() {
        return CEPCliente;
    }

    public void setCEPCliente(String CEPCliente) {
        this.CEPCliente = CEPCliente;
    }

    public int getnPtosServer() {
        return nPtosServer;
    }

    public void setnPtosServer(int nPtosServer) {
        this.nPtosServer = nPtosServer;
    }

    public int getnPtosCliente() {
        return nPtosCliente;
    }

    public void setnPtosCliente(int nPtosCliente) {
        this.nPtosCliente = nPtosCliente;
    }

    public int getnTentativas() {
        return nTentativas;
    }

    public void setnTentativas(int nTentativas) {
        this.nTentativas = nTentativas;
    }

    public boolean isFimServer() {
        return isFimServer;
    }

    public void setFimServer(boolean fimServer) {
        isFimServer = fimServer;
    }

    public boolean isFimCliente() {
        return isFimCliente;
    }

    public void setFimCliente(boolean fimCliente) {
        isFimCliente = fimCliente;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
    }


}
