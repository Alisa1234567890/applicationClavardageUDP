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
            this.socketServeur = new DatagramSocket(null);
            // bind de l'adresse sur le socket principal
            InetSocketAddress adresse = new InetSocketAddress("localhost", port);
            socketServeur.bind(adresse);

            Map<String, ClientInfo> clients = new Map<>();
            // attente active
            while(!socketServeur.isClosed()) {
                byte[] recues = new byte[1024]; // tampon d'émission
                byte[] envoyees; // tampon de réception
                // 3 - Recevoir
                DatagramPacket paquetRecu = new DatagramPacket(recues, recues.length);
                socketServeur.receive(paquetRecu);
                String message = new String(paquetRecu.getData(), 0, paquetRecu.getLength());
                System.out.println("Reçu: " + message);

                // 4 - créer un DatagramSocket pour le client sur le PORT <n>
                DatagramSocket socketServeur = new DatagramSocket(null);
                InetSocketAddress adresse = new InetSocketAddress("localhost", 9001); // TODO : comment générer le port ?
                socketServeur.bind(adresse);

                // 5 - notifie le client du port alloué via le message PORT:<n>
                InetAddress adrClient = paquetRecu.getAddress();
                int prtClient = paquetRecu.getPort();
                String reponse = "Accusé de réception";
                envoyees = reponse.getBytes();
                DatagramPacket paquetEnvoye = new DatagramPacket(envoyees, envoyees.length, adrClient, prtClient);
                socketServeur.send(paquetEnvoye);

                // 6 - Enregistrer le client et démarrer un GestionnaireClient
                clients.add()
                GestionnaireClient gc = new GestionnaireClient();
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