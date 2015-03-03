// ---------------------------------------------------------------------------
// jWebSocket - < Description/Name of the Module >
// Copyright(c) 2010-212 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.watchdog.notifier;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.mail.SimpleEmail;
import org.jwebsocket.watchdog.api.INotifier;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public class MailNotifier implements INotifier {

    //attributes
    private String mHostName;
    private String mPort;
    private String mSubject;
    private String mID;
    private String mDescription;
    private String mFrom;
    private List<String> mUsersList;

    /**
     * Getter
     */
    public String getFrom() {
        return mFrom;
    }

    public String getHostName() {
        return mHostName;
    }

    public String getPort() {
        return mPort;
    }

    public String getSubject() {
        return mSubject;
    }

    public List<String> getUsersList() {
        return mUsersList;
    }

    //Setter
    public void setFrom(String aFrom) {
        this.mFrom = aFrom;
    }

    public void setHostName(String aHostName) {
        this.mHostName = aHostName;
    }

    public void setPort(String aPort) {
        this.mPort = aPort;
    }

    public void setSubject(String aSubject) {
        this.mSubject = aSubject;
    }

    public void setUsersList(List<String> aUsersList) {
        this.mUsersList = aUsersList;
    }

    //Overriding methods of the INotifier interface
    @Override
    public String getId() {
        return mID;
    }

    @Override
    public void setId(String aId) {
        this.mID = aId;
    }

    @Override
    public String getDescription() {
        return mDescription;
    }

    @Override
    public List<String> getTo() {
        return mUsersList;
    }

    @Override
    public void setTo(List<String> aTo) {
        this.mUsersList = aTo;
    }

    @Override
    public void notify(String aMessage) {
        try {
            sendMail(aMessage);
        } catch (Exception ex) {
            Logger.getLogger(MailNotifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setDescription(String aDescription) {
        this.mDescription = aDescription;
    }

    public void initialize() throws Exception {
    }

    public void shutdown() throws Exception {
    }

    private void sendMail(String aMessage) throws Exception {
        //send a Simple e-mail receiving the message


        SimpleEmail lSm = new SimpleEmail();
        //Creating simple mail 

        lSm.setHostName(mHostName);
        //Hostname or ip address

        lSm.setSSL(false);
        //Use Certifacate SSL

        lSm.setSslSmtpPort(mPort);
        //Choosing the port

        for (int i = 0; i < mUsersList.size(); i++) {
            lSm.addTo(mUsersList.get(i));
            //Authentication
            //sm.setAuthentication("lzaila", "");
            //Whom you will send the e-mail
        }

        lSm.setFrom(mFrom);
        //the e-mail was send from

        lSm.setSubject(mSubject);
        //subject of message

        lSm.setMsg(aMessage);
        //and the body of the message
        //sm.setMsg("if you are receiving this message probably the 
        //server is presenting malfunctioning");

        lSm.send();
        //send the message
    }
}
