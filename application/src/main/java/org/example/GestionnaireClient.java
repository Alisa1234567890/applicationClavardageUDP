package org.example;

import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentHashMap;

public class GestionnaireClient implements Runnable {
    private ClientInfo clientInfo;
    private DatagramSocket socket;
    private ConcurrentHashMap<String, ClientInfo> clients;
    GestionnaireClient() {
        clients = new ConcurrentHashMap<>();
        clientInfo = null;
        socket = null;
    }
    ClientInfo getClientInfo() {
        return clientInfo;
    }
    void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }
    DatagramSocket getSocket() {
        return socket;
    }
    void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }
    ConcurrentHashMap<String, ClientInfo> getClients() {
        return clients;
    }
    void setClients(ConcurrentHashMap<String, ClientInfo> clients) {
        this.clients = clients;
    }

    @Override
    public void run() {

    }
}
