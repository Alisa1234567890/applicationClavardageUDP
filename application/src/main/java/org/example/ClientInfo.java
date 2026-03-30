package org.example;

public class ClientInfo {
    private String pseudo;
    private String adresseIP;
    private int port;

    ClientInfo(String pseudo, String adresseIP, int port) {
        this.pseudo = pseudo;
        this.adresseIP = adresseIP;
        this.port = port;
    }

    String getPseudo() {
        return pseudo;
    }
    String getAdresseIP() {
        return adresseIP;
    }
    int getPort() {
        return port;
    }
    void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }
    void setAdresseIP(String adresseIP) {
        this.adresseIP = adresseIP;
    }
    void setPort(int port) {
        this.port = port;
    }
}
