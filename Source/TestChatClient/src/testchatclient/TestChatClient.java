/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testchatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPasswordField;

/**
 *
 * @author yeula
 */
public class TestChatClient {

    private final int serverPort;
    private final String serverName;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;

    ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public TestChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        TestChatClient client = new TestChatClient("localhost", 8818);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);

            }

        });
        
        client.addMessageListener(new MessageListener(){
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("You got a message from " + fromLogin + "===>" + msgBody);
            }
        
        });
        if (!client.connect()) {
            System.err.println("Can't connect to server");
        } else {
            System.out.println("Connect successfull");
            if (client.login("guest", "guest")) {
                System.out.println("Login successfull");
                client.msg("thien", "hello world!!");
            } else {
                System.err.println("Loging failed");
            }
            
//            client.logoff();
        }
    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is: " + this.socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException ex) {
            Logger.getLogger(TestChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean login(String user, String pass) throws IOException {
        String cmd = "login " + user + " " + pass + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        if ("login successfully".equalsIgnoreCase(response)) {
            startMessageReader();
            
            return true;
        } else {
            return false;
        }
    }

    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    private void startMessageReader() {
        
        Thread t = new Thread() {
            @Override
            public void run() {
                
                readMessageLoop();
                
            }

        };

        t.start();
    }

    private void readMessageLoop() {
        String line;

        try {
            while ((line = bufferedIn.readLine()) != null) {

                String[] tokens = line.split(" ");

                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if("online".equalsIgnoreCase(cmd)){
                        handleOnline(tokens);
                    }else if("offline".equalsIgnoreCase(cmd)){
                        handleOffline(tokens);
                    }else if("msg".equalsIgnoreCase(cmd)){
                        handleMessage(tokens);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TestChatClient.class.getName()).log(Level.SEVERE, null, ex);
            try {
                socket.close();
            } catch (IOException ex1) {
                Logger.getLogger(TestChatClient.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener: userStatusListeners){
            listener.online(login);
        }
                
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener: userStatusListeners){
            listener.offline(login);
        }
    }

    public void logoff() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }
    
    public void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }
    
    private void removeMessageListener(MessageListener listener){
        messageListeners.remove(listener);
    }

    private void handleMessage(String[] tokens) {
        String user = tokens[1];
        String msgBody = getMessageBody(tokens);

        for(MessageListener listener: messageListeners){
            listener.onMessage(user, msgBody);
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

    boolean register(String user, String pass) throws IOException {
        String cmd = "register " + user + " " + pass + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        if ("register successfully".equalsIgnoreCase(response)) {
            startMessageReader();
            
            return true;
        } else {
            return false;
        }
    }

    
}
