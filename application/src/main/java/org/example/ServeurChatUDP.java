package org.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class ServeurChatUDP {
    public static void main(String[] args) {
        try{
            DatagramSocket socketServeur = new DatagramSocket(null);
            InetSocketAddress adresse = new InetSocketAddress("localhost", 9000);
            socketServeur.bind(adresse);
            byte[] recues = new byte[1024]; // tampon d'émission
            byte[] envoyees; // tampon de réception
            // 3 - Recevoir
            DatagramPacket paquetRecu = new DatagramPacket(recues, recues.length);
            socketServeur.receive(paquetRecu);
            String message = new String(paquetRecu.getData(), 0, paquetRecu.getLength());
            System.out.println("Reçu: " + message);
            // 4 - Émettre
            InetAddress adrClient = paquetRecu.getAddress(); int prtClient = paquetRecu.getPort();
            String reponse = "Accusé de réception"; envoyees = reponse.getBytes();
            DatagramPacket paquetEnvoye = new DatagramPacket(envoyees, envoyees.length, adrClient, prtClient);
            socketServeur.send(paquetEnvoye);
            // 5 - Libérer le canal
            socketServeur.close();
            } catch(Exception e){
                System.err.println(e);
            }
    }
}