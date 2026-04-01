package org.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gère les échanges avec UN client (stocké sous clientInfo)
 */
public class GestionnaireClient implements Runnable {
    private static final int BUFFER_SIZE = 1024;

    private ClientInfo clientInfo;
    private DatagramSocket socket; // Socket du client
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

    /**
     * Code exécuté dans le thread
     */
    @Override
    public void run() {
        String pseudo = clientInfo.getPseudo();
        clients.putIfAbsent(pseudo, clientInfo);

        try {
            // Pour chaque client connecté sur le serveur, envoi le message d'arrivée de `clientInfo`
            byte[] envoyees = ("[SERVEUR] " + pseudo + " a rejoint le chat.").getBytes(StandardCharsets.UTF_8);
            for (String pseudoDest : clients.keySet()) {
                ClientInfo destinataire = clients.get(pseudoDest);
                if (destinataire == null) {
                    continue;
                }
                InetAddress adrClient = InetAddress.getByName(destinataire.getAdresseIP());
                int prtClient = destinataire.getPort();
                DatagramPacket paquetEnvoye = new DatagramPacket(envoyees, envoyees.length, adrClient, prtClient);
                socket.send(paquetEnvoye);
            }

            // écoute en boucle, tant que le socket est actif
            while (!socket.isClosed()) {
                // Reçevoir le paquet
                byte[] recues = new byte[BUFFER_SIZE];
                DatagramPacket paquetRecu = new DatagramPacket(recues, recues.length);
                socket.receive(paquetRecu); // attente du paquet : tant qu'il n'y a rien, le thread est bloqué

                String message = new String(paquetRecu.getData(), 0, paquetRecu.getLength(), StandardCharsets.UTF_8).trim();
                if (message.isEmpty()) {
                    continue;
                }

                // feature N1: Vérifier si le message est '/liste' et retourner la liste des connectés
                if ("/liste".equalsIgnoreCase(message)) {
                    String liste = "[SERVEUR] Clients connectés : " + String.join(", ", clients.keySet());
                    byte[] listeBytes = liste.getBytes(StandardCharsets.UTF_8);
                    InetAddress adrClient = InetAddress.getByName(clientInfo.getAdresseIP());
                    int prtClient = clientInfo.getPort();
                    DatagramPacket paquetListe = new DatagramPacket(listeBytes, listeBytes.length, adrClient, prtClient);
                    socket.send(paquetListe);
                    continue;
                }

                // Vérifier si le message est 'EXIT' et le traiter en conséquence
                if ("EXIT".equalsIgnoreCase(message)) {
                    clients.remove(pseudo);
                    byte[] depart = ("[SERVEUR] " + pseudo + " a quitte le chat.").getBytes(StandardCharsets.UTF_8);

                    for (String pseudoDest : clients.keySet()) {
                        ClientInfo destinataire = clients.get(pseudoDest);
                        if (destinataire == null) {
                            continue;
                        }
                        InetAddress adrClient = InetAddress.getByName(destinataire.getAdresseIP());
                        int prtClient = destinataire.getPort();
                        DatagramPacket paquetEnvoye = new DatagramPacket(depart, depart.length, adrClient, prtClient);
                        socket.send(paquetEnvoye);
                    }
                    break;
                }

                byte[] chat = (pseudo + " : " + message).getBytes(StandardCharsets.UTF_8);
                for (String pseudoDest : clients.keySet()) {
                    if (pseudo.equals(pseudoDest)) {
                        continue;
                    }
                    ClientInfo destinataire = clients.get(pseudoDest);
                    if (destinataire == null) {
                        continue;
                    }
                    InetAddress adrClient = InetAddress.getByName(destinataire.getAdresseIP());
                    int prtClient = destinataire.getPort();
                    DatagramPacket paquetEnvoye = new DatagramPacket(chat, chat.length, adrClient, prtClient);
                    socket.send(paquetEnvoye);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            clients.remove(pseudo);
            if (!socket.isClosed()) {
                socket.close();
            }
        }
    }
}
