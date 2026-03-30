package org.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class GestionnaireClient implements Runnable {
    private static final int BUFFER_SIZE = 1024;

    private ClientInfo clientInfo;
    private DatagramSocket socket;
    private ConcurrentHashMap<String, ClientInfo> clients;

    GestionnaireClient() {
        this.clients = new ConcurrentHashMap<>();
        this.clientInfo = null;
        this.socket = null;
    }

    public GestionnaireClient(ClientInfo clientInfo, DatagramSocket socket,
                              ConcurrentHashMap<String, ClientInfo> clients) {
        if (clientInfo == null || socket == null || clients == null) {
            throw new IllegalArgumentException("clientInfo, socket et clients sont obligatoires");
        }
        this.clientInfo = clientInfo;
        this.socket = socket;
        this.clients = clients;
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
        String pseudo = clientInfo.getPseudo();
        clients.putIfAbsent(pseudo, clientInfo);

        byte[] bienvenue = ("[SERVEUR] " + pseudo + " a rejoint le chat.").getBytes(StandardCharsets.UTF_8);
        clients.forEach((pseudoDest, info) -> {
            try {
                InetAddress adresse = InetAddress.getByName(info.getAdresseIP());
                DatagramPacket paquet = new DatagramPacket(bienvenue, bienvenue.length, adresse, info.getPort());
                socket.send(paquet);
            } catch (IOException e) {
                System.err.println("Diffusion impossible vers " + pseudoDest + " : " + e.getMessage());
            }
        });

        try {
            while (!socket.isClosed()) {
                byte[] recues = new byte[BUFFER_SIZE];
                DatagramPacket paquetRecu = new DatagramPacket(recues, recues.length);

                socket.receive(paquetRecu);

                String message = new String(paquetRecu.getData(), 0, paquetRecu.getLength(), StandardCharsets.UTF_8).trim();
                if (message.isEmpty()) {
                    continue;
                }

                if ("EXIT".equalsIgnoreCase(message)) {
                    clients.remove(pseudo);

                    byte[] depart = ("[SERVEUR] " + pseudo + " a quitte le chat.").getBytes(StandardCharsets.UTF_8);
                    clients.forEach((pseudoDest, info) -> {
                        try {
                            InetAddress adresse = InetAddress.getByName(info.getAdresseIP());
                            DatagramPacket paquet = new DatagramPacket(depart, depart.length, adresse, info.getPort());
                            socket.send(paquet);
                        } catch (IOException e) {
                            System.err.println("Diffusion impossible vers " + pseudoDest + " : " + e.getMessage());
                        }
                    });
                    break;
                }

                byte[] chat = (pseudo + " : " + message).getBytes(StandardCharsets.UTF_8);
                clients.forEach((pseudoDest, info) -> {
                    if (pseudo.equals(pseudoDest)) {
                        return;
                    }
                    try {
                        InetAddress adresse = InetAddress.getByName(info.getAdresseIP());
                        DatagramPacket paquet = new DatagramPacket(chat, chat.length, adresse, info.getPort());
                        socket.send(paquet);
                    } catch (IOException e) {
                        System.err.println("Diffusion impossible vers " + pseudoDest + " : " + e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.err.println("Erreur de reception pour " + pseudo + " : " + e.getMessage());
            }
        } finally {
            clients.remove(pseudo);
            if (!socket.isClosed()) {
                socket.close();
            }
        }
    }
}
