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
public interface UserStatusListener {
    public void online(String online);
    public void offline(String offline);
}
