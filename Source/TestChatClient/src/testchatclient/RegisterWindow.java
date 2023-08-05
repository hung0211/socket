/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testchatclient;

import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 *
 * @author yeula
 */
public class RegisterWindow extends JFrame{
    JTextField loginField = new JTextField(30);
    JPasswordField passwordField = new JPasswordField();
    JPasswordField confirmPasswordField = new JPasswordField();

    JButton registerButton = new JButton("Register");
    private final TestChatClient client;
    
    public RegisterWindow(){
        super("Register");
        this.client = new TestChatClient("localhost", 8818);
        client.connect();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(loginField);
        p.add(passwordField);
        p.add(confirmPasswordField);
        p.add(registerButton);
        
        getContentPane().add(p, BorderLayout.CENTER);
    }
    
}
