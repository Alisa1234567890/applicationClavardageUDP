package org.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientChatUDP {
    public static void main(String[] args) {
        // nb : un client n'a pas accès à ses ports
        try {
            // 1 - Création du canal
            DatagramSocket socketClient = new DatagramSocket();
            InetAddress adresseServeur = InetAddress.getByName("localhost");
            byte[] envoyees; // tampon d'émission
            byte[] recues = new byte[1024]; // tampon de réception

            // 1 - Demander le pseudo à l'utilisateur
            Scanner scanner = new Scanner(System.in);
            System.out.print("Entrez votre pseudo : ");
            String pseudo = scanner.nextLine();

            // 2 - Émettre JOIN:<pseudo> au serveur sur le port principal (9000)
            envoyees = ("JOIN:" + pseudo).getBytes();
            DatagramPacket messageEnvoye = new DatagramPacket(envoyees, envoyees.length, adresseServeur, 9000);
            socketClient.send(messageEnvoye);

            // 3 - Recevoir PORT:<n> et retenir le port dédié
            DatagramPacket paquetRecu = new DatagramPacket(recues, recues.length);
            socketClient.receive(paquetRecu);
            String reponse = new String(paquetRecu.getData(), 0, paquetRecu.getLength());
            int portDedie = Integer.parseInt(reponse.replace("PORT:", "").trim());
            System.out.println("Connecté sur le port dédié : " + portDedie);

            // 4 - Démarrer un thread d'écoute qui reçoit et affiche les messages entrants
            Thread threadEcoute = new Thread(() -> {
                try {
                    while (!socketClient.isClosed()) {
                        byte[] recuesEcoute = new byte[1024];
                        DatagramPacket paquetEntrant = new DatagramPacket(recuesEcoute, recuesEcoute.length);
                        socketClient.receive(paquetEntrant);
                        String messageRecu = new String(paquetEntrant.getData(), 0, paquetEntrant.getLength());
                        System.out.println(messageRecu);
                    }
                } catch (Exception e) {
                    if (!socketClient.isClosed()) {
                        System.err.println(e);
                    }
                }
            });
            threadEcoute.setDaemon(true);
            threadEcoute.start();

            // 5 - Lire en boucle les messages saisis et les envoyer sur le port dédié
            String messageSaisi;
            while (true) {
                messageSaisi = scanner.nextLine();

                // 6 - Si exit : envoyer EXIT, fermer la socket et quitter
                if ("exit".equalsIgnoreCase(messageSaisi)) {
                    envoyees = "EXIT".getBytes();
                    DatagramPacket paquetExit = new DatagramPacket(envoyees, envoyees.length, adresseServeur, portDedie);
                    socketClient.send(paquetExit);
                    break;
                }

                envoyees = messageSaisi.getBytes();
                DatagramPacket paquetMessage = new DatagramPacket(envoyees, envoyees.length, adresseServeur, portDedie);
                socketClient.send(paquetMessage);
            }

            // Libérer le canal
            socketClient.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
