package org.example.fooddelivery;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FoodDeliveryServer {
    private static final int PORT = 1234;

    public static void main(String[] args) throws IOException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Food Delivery Server running on port " + PORT);

            while (true){
                Socket clientSocket = serverSocket.accept();
                executorService.execute(new ClientHandler(clientSocket));
            }

        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            executorService.shutdown();
        }
    }
}
