/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testchatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yeula
 */
public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private String user;
    private Server server;
    private OutputStream outputStream;

    private HashSet<String> topicSet = new HashSet<String>();
    

    public String getUser() {
        return user;
    }

    ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");

            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];

                if ("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else if ("register".equalsIgnoreCase(cmd)) {
                    handleRegister(outputStream, tokens);
                }else if ("msg".equalsIgnoreCase(cmd)) {
                    handleMessage(tokens);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                } else if ("leave".equalsIgnoreCase(cmd)) {
                    handleLeave(tokens);
                } else {
                    String msg = "unknown" + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }

            }

        }

    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String user = tokens[1];
            String pass = tokens[2];
            System.out.println(user);
            System.out.println(pass);

            if (user.equals("guest") && pass.equals("guest") || user.equals("thien") && pass.equals("thien")) {
                String msg = "login successfully\n";
                outputStream.write(msg.getBytes());
                this.user = user;
                System.out.println("User login: " + user);

                String onlineMsg = "online " + user + "\n";
                ArrayList<ServerWorker> workerList = server.getWorkerList();
                //send current user to other online user
                for (ServerWorker worker : workerList) {
                    if (this.user.equals(worker.user)) {
                        continue;
                    }
                    worker.send(onlineMsg);
                }

                //send other online user to current user's state
                for (ServerWorker worker : workerList) {
                    if (this.user.equals(worker.user)) {
                        continue;
                    }
                    String msg2 = "online " + worker.getUser() + "\n";
                    this.send(msg2);
                }
            } else {
                String msg = "login fail\n";
                outputStream.write(msg.getBytes());
                System.err.println("login fail for " + user);
            }
        }
    }

    private void send(String msg) throws IOException {
        if (user != null) {
            outputStream.write(msg.getBytes());

        }
    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);

        String offlineMsg = "offline " + user + "\n";
        ArrayList<ServerWorker> workerList = server.getWorkerList();

        for (ServerWorker worker : workerList) {
            if (this.user.equals(worker.user)) {
                continue;
            }
            worker.send(offlineMsg);
        }

        clientSocket.close();

    }

    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = getMessageBody(tokens);

        boolean isTopic = sendTo.charAt(0) == '#';

        ArrayList<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker : workerList) {

            if (isTopic) {
                if (worker.isMemberOfTopic(sendTo)) {
                    String outMsg = "msg " + sendTo + ":" + user + " " + body + "\n";
                    worker.send(outMsg);
                }
            } else if (sendTo.equals(worker.user)) {
                String outMsg = "msg " + user + " " + body + "\n";
                worker.send(outMsg);
            }

        }
    }

    private String getMessageBody(String[] tokens) {
        List<String> list = new ArrayList<String>(Arrays.asList(tokens));
        System.out.println(list.toString());
        list.remove(0);
        list.remove(0);
        System.out.println(list.toString());
        String Msg = String.join(" ", list);
        return Msg;
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    private boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    private void handleRegister(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String user = tokens[1];
            String pass = tokens[2];
            System.out.println(user);
            System.out.println(pass);

            if (server.listUser.get(user) == null) {
                String msg = "register successfully\n";
                outputStream.write(msg.getBytes());
                
                server.listUser.put(user, pass);
                System.out.println("Register u: " + user);

                String onlineMsg = "online " + user + "\n";
                ArrayList<ServerWorker> workerList = server.getWorkerList();
                //send current user to other online user
                for (ServerWorker worker : workerList) {
                    if (this.user.equals(worker.user)) {
                        continue;
                    }
                    worker.send(onlineMsg);
                }

                //send other online user to current user's state
                for (ServerWorker worker : workerList) {
                    if (this.user.equals(worker.user)) {
                        continue;
                    }
                    String msg2 = "online " + worker.getUser() + "\n";
                    this.send(msg2);
                }
            } else {
                String msg = "register fail\n";
                outputStream.write(msg.getBytes());
                System.err.println("login fail for " + user);
            }
        }
    }

}
