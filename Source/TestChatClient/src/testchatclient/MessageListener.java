/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testchatclient;

/**
 *
 * @author yeula
 */
public interface MessageListener {
    public void onMessage(String fromLogin, String msgBody);
}
