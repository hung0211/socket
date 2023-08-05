/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testchatserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author yeula
 */
public class Server extends Thread {

    private final int serverPort;
    private ArrayList<ServerWorker> workerList = new ArrayList<ServerWorker>();
    HashMap<String, String> listUser = new HashMap<String, String>();

    public ArrayList<ServerWorker> getWorkerList() {
        return workerList;
    }

    Server(int serverPort) {
        this.serverPort = serverPort;
        listUser.put("guest", "guest");
        listUser.put("hung", "hung");
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                System.out.println("About to accept client connection....");
                Socket clientSocket = serverSocket.accept();
                if (clientSocket != null) {
                    System.out.println("Accepted collection form " + clientSocket);
                    ServerWorker worker = new ServerWorker(this, clientSocket);
                    workerList.add(worker);
                    worker.start();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void removeWorker(ServerWorker worker) {
        workerList.remove(worker);
    }

}
