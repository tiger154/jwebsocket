/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.watchdog.notifier;

import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author lester
 */
public class MailNotifierTest extends TestCase {
    
    public MailNotifierTest(String testName) {
        super(testName);
    }

    /**
     * Test of getFrom method, of class MailNotifier.
     */
    public void testGetFrom() {
        System.out.println("getFrom");
        MailNotifier instance = new MailNotifier();
        String expResult = "admin@hab.uci.cu";
        instance.setFrom(expResult);
        String result = instance.getFrom();
        assertEquals(expResult, result);
    }

    /**
     * Test of getHostName method, of class MailNotifier.
     */
    public void testGetHostName() {
        System.out.println("getHostName");
        MailNotifier instance = new MailNotifier();
        String expResult = "10.208.0.44";
        instance.setHostName(expResult);
        String result = instance.getHostName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPort method, of class MailNotifier.
     */
    public void testGetPort() {
        System.out.println("getPort");
        MailNotifier instance = new MailNotifier();
        String expResult = "465";
        instance.setPort(expResult);
        String result = instance.getPort();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getSubject method, of class MailNotifier.
     */
    public void testGetSubject() {
        System.out.println("getSubject");
        MailNotifier instance = new MailNotifier();
        String expResult = "Warning!!!";
        instance.setSubject(expResult);
        String result = instance.getSubject();
        assertEquals(expResult, result);
       
    }

    /**
     * Test of getUsersList method, of class MailNotifier.
     */
    public void testGetUsersList() {
        System.out.println("getUsersList");
        MailNotifier instance = new MailNotifier();
        List expResult = null;
        List result = instance.getUsersList();
        assertEquals(expResult, result);
       
    }

    /**
     * Test of setFrom method, of class MailNotifier.
     */
    public void testSetFrom() {
        System.out.println("setFrom");
        String from = "";
        MailNotifier instance = new MailNotifier();
        instance.setFrom(from);
       
    }

    /**
     * Test of setHostName method, of class MailNotifier.
     */
    public void testSetHostName() {
        System.out.println("setHostName");
        String hostName = "";
        MailNotifier instance = new MailNotifier();
        instance.setHostName(hostName);
       
    }

    /**
     * Test of setPort method, of class MailNotifier.
     */
    public void testSetPort() {
        System.out.println("setPort");
        String port = "";
        MailNotifier instance = new MailNotifier();
        instance.setPort(port);
        
    }

    /**
     * Test of setSubject method, of class MailNotifier.
     */
    public void testSetSubject() {
        System.out.println("setSubject");
        String subject = "";
        MailNotifier instance = new MailNotifier();
        instance.setSubject(subject);
        
    }

    /**
     * Test of setUsersList method, of class MailNotifier.
     */
    public void testSetUsersList() {
        System.out.println("setUsersList");
        List<String> usersList = null;
        MailNotifier instance = new MailNotifier();
        instance.setUsersList(usersList);
        
    }

    /**
     * Test of getId method, of class MailNotifier.
     */
    public void testGetId() {
        System.out.println("getId");
        MailNotifier instance = new MailNotifier();
        String expResult = "1";
        instance.setId(expResult);
        String result = instance.getId();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of setId method, of class MailNotifier.
     */
    public void testSetId() {
        System.out.println("setId");
        String Id = "";
        MailNotifier instance = new MailNotifier();
        instance.setId(Id);
       
    }

    /**
     * Test of getDescription method, of class MailNotifier.
     */
    public void testGetDescription() {
        System.out.println("getDescription");
        MailNotifier instance = new MailNotifier();
        String expResult = "Testingggg";
        instance.setDescription(expResult);
        String result = instance.getDescription();
        assertEquals(expResult, result);
       
    }

    /**
     * Test of getTo method, of class MailNotifier.
     */
    public void testGetTo() {
        System.out.println("getTo");
        MailNotifier instance = new MailNotifier();
        List expResult = null;
        List result = instance.getTo();
        assertEquals(expResult, result);
       
    }

    /**
     * Test of setTo method, of class MailNotifier.
     */
    public void testSetTo() {
        System.out.println("setTo");
        List<String> to = null;
        MailNotifier instance = new MailNotifier();
        instance.setTo(to);
        
    }

    /**
     * Test of notify method, of class MailNotifier.
     */
    public void testNotify() {
        System.out.println("notify");
        String message = "";
        MailNotifier instance = new MailNotifier();
        instance.notify(message);
       
    }

    /**
     * Test of setDescription method, of class MailNotifier.
     */
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        MailNotifier instance = new MailNotifier();
        instance.setDescription(description);
       
    }

    /**
     * Test of initialize method, of class MailNotifier.
     */
    public void testInitialize() throws Exception {
        System.out.println("initialize");
        MailNotifier instance = new MailNotifier();
        instance.initialize();
        
    }

    /**
     * Test of shutdown method, of class MailNotifier.
     */
    public void testShutdown() throws Exception {
        System.out.println("shutdown");
        MailNotifier instance = new MailNotifier();
        instance.shutdown();
        
    }
}
