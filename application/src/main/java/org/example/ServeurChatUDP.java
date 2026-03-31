package org.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class ServeurChatUDP {
    public static void main(String[] args) {
        DatagramSocket socketServeur = null;
        try {
            /** 1- Création du DatagramSocket (port 9000) **/
            socketServeur = new DatagramSocket(null);
            InetSocketAddress adresse = new InetSocketAddress("localhost", 9000);
            socketServeur.bind(adresse);

            ConcurrentHashMap<String, ClientInfo> clients = new ConcurrentHashMap<>();

            /** 2- Attente active des datagrammes **/
            while (!socketServeur.isClosed()) {
                byte[] recues = new byte[1024]; // tampon d'émission
                byte[] envoyees; // tampon de réception

                /** 3- Reception **/
                DatagramPacket paquetRecu = new DatagramPacket(recues, recues.length);
                socketServeur.receive(paquetRecu);
                String message = new String(paquetRecu.getData(), 0, paquetRecu.getLength());
                System.out.println("Reçu: " + message);

                if (message.startsWith("JOIN:")) {
                    /** 3.a- créer un DatagramSocket pour le client sur le PORT <n> **/
                    DatagramSocket socketClient = new DatagramSocket(0); // port libre côté serveur
                    int portServeurClient = socketClient.getLocalPort(); // port alloué côté serveur

                    // On construit d'abord le client avec les données du paquet
                    String pseudo = message.substring(5);                      // prénom du client
                    InetAddress adrClient = paquetRecu.getAddress();           // IP du client
                    int prtClient = paquetRecu.getPort();                      // port du client, côté client

                    /** 3.b- notifie le client du port alloué via le message PORT:<n> **/
                    String reponse = "PORT:" + portServeurClient;
                    envoyees = reponse.getBytes();
                    DatagramPacket paquetEnvoye = new DatagramPacket(envoyees, envoyees.length, adrClient, prtClient);
                    socketServeur.send(paquetEnvoye);

                    /** 3.c- Enregistrer le client et créer un Gestionnaire dans un nouveau thread **/
                    ClientInfo clientInfo = new ClientInfo(pseudo, adrClient.getHostAddress(), prtClient);
                    // Puis on ajoute le client à la liste
                    clients.put(pseudo, clientInfo);
                    new Thread(new GestionnaireClient(clientInfo, socketClient, clients)).start();
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            // 5 - Libérer le canal
            if (socketServeur != null && !socketServeur.isClosed()) {
                socketServeur.close();
            }
        }
    }
}