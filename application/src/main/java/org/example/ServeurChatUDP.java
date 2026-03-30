package org.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ServeurChatUDP {
    private final int port=9000;
    private DatagramSocket socketServeur; // le DatagramSocket principal (celui du serveur, port 9000)
    private Map<String, ClientInfo> clients;

    public ServeurChatUDP(){}

    public static void main(String[] args) {
        try {
            /** 1- Création du DatagramSocket (port 9000) **/
            this.socketServeur = new DatagramSocket(null);
            // bind de l'adresse sur le socket principal
            InetSocketAddress adresse = new InetSocketAddress("localhost", port);
            socketServeur.bind(adresse);

            Map<String, ClientInfo> clients = new Map<>();

            /** 2- Attente active des datagrammes **/
            while(!socketServeur.isClosed()) {
                byte[] recues = new byte[1024]; // tampon d'émission
                byte[] envoyees; // tampon de réception

                /** 3- Reception **/
                DatagramPacket paquetRecu = new DatagramPacket(recues, recues.length);
                socketServeur.receive(paquetRecu);
                String message = new String(paquetRecu.getData(), 0, paquetRecu.getLength());
                System.out.println("Reçu: " + message);

                if (message.startsWith("JOIN:")) {
                    /** 3.a- créer un DatagramSocket pour le client sur le PORT <n> **/
                    DatagramSocket socketClient = new DatagramSocket(0); // port du client, libre côté serveur
                    int portServeurClient = socketClient.getLocalPort(); // port du client, alloué côté serveur

                    /** 3.b- notifie le client du port alloué via le message PORT:<n> **/
                    String reponse = "Port alloué : " + portServeurDedie;
                    envoyees = reponse.getBytes();
                    DatagramPacket paquetEnvoye = new DatagramPacket(envoyees, envoyees.length, adrClient, prtClient);
                    socketServeur.send(paquetEnvoye);

                    /** 3.c- Enregistrer le client et créer un Gestionnaire dans un nouveau thread **/
                    // On construit d'abord le client avec les données du paquet
                    String pseudo = message.substring(5);         // prénom du client
                    InetAddress adrClient = paquet.getAddress();  // IP du client
                    int prtClient = paquet.getPort();             // port du client, côté client
                    ClientInfo clientInfo = new ClientInfo(pseudo, adrClient, prtClient);
                    // Puis on ajoute le client à la liste
                    clients.put(pseudo, clientInfo);
                    new Thread(new GestionnaireClient(adrClient, portServeurClient, clients)).start()
                }
            }
        } catch(Exception e) {
            System.err.println(e);
        } finally {
            // 5 - Libérer le canal
            if (!socketServeur.isClosed()) {
                socketServeur.close();
            }
        }
    }
}