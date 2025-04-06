package org.example.fooddelivery;

import java.net.Socket;

public class SClientHandler extends Thread{
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    public void start() {
    }
}
