package org.jwebsocket.watchdog.notifier;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.mail.SimpleEmail;
import org.jwebsocket.watchdog.api.INotifier;

/**
 *
 * @author lester
 */
public class MailNotifier implements INotifier {

    //attributes
    private String hostName;
    private String port;
    private String subject;
    private String id;
    private String description;
    private String from;
    List<String> usersList;

    /**
     * Getter
     */
    public String getFrom() {
        return from;
    }

    public String getHostName() {
        return hostName;
    }

    public String getPort() {
        return port;
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getUsersList() {
        return usersList;
    }

    //Setter
    public void setFrom(String from) {
        this.from = from;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setUsersList(List<String> usersList) {
        this.usersList = usersList;
    }

    //Overriding methods of the INotifier interface
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String Id) {
        this.id = Id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<String> getTo() {
        return usersList;
    }

    @Override
    public void setTo(List<String> to) {
        this.usersList = to;
    }

    @Override
    public void notify(String message) {
        try {
            sendMail(message);
        } catch (Exception ex) {
            Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void initialize() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
    }

    //send a Simple e-mail receiving the message
    private void sendMail(String message) throws Exception {

        //Creating simple mail 
        SimpleEmail sm = new SimpleEmail();
        //Hostname or ip address
        sm.setHostName(hostName);
        //Use Certifacate SSL
        sm.setSSL(false);
        //Choosing the port
        sm.setSslSmtpPort(port);
        //Authentication
        // sm.setAuthentication("lzaila", "");
        //Who you will send the e-mail
        for (int i = 0; i < usersList.size(); i++) {
            sm.addTo(usersList.get(i));
        }
        //the e-mail was send from
        sm.setFrom(from);
        //subject of message

        sm.setSubject(subject);
        //and the body of the message
        //sm.setMsg("if you are receiving this message probably the server is presenting malfunctioning");
        sm.setMsg(message);
        //send the message
        sm.send();
    }
}
