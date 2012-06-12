// ---------------------------------------------------------------------------
// jWebSocket - < MailNotifier >
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
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

    private String mHostName;
    private String mPort;
    private String mSubject;
    private String mID;
    private String mDescription;
    private String mFrom;
    private List<String> mUsersList;
    private SimpleEmail mMailer;

    /**
     * Get from the email was sent
     * 
     * @return 
     */
    public String getFrom() {
        return mFrom;
    }

    /**
     * Get the host where the e-mail will be send
     * 
     * @return 
     */
    public String getHostName() {
        return mHostName;
    }

    /**
     * Get the port of the host
     * 
     * @return 
     */
    public String getPort() {
        return mPort;
    }

    /**
     * Get the subject of the email
     * 
     * @return 
     */
    public String getSubject() {
        return mSubject;
    }

    /**
     * Get the user lists whom receive the e-mail notifications
     * 
     * @return 
     */
    public List<String> getUsersList() {
        return mUsersList;
    }

    /**
     * Set the e-mail was sent for 
     * 
     * @param aFrom 
     */
    public void setFrom(String aFrom) {
        this.mFrom = aFrom;
    }

    /**
     * Set the host name of the email server
     * 
     * @param aHostName 
     */
    public void setHostName(String aHostName) {
        this.mHostName = aHostName;
    }

    /**
     * Set the port to stablish connection with the server.
     * 
     * @param aPort 
     */
    public void setPort(String aPort) {
        this.mPort = aPort;
    }

    /**
     * Set the subject of the e-mail.
     * 
     * @param aSubject 
     */
    public void setSubject(String aSubject) {
        this.mSubject = aSubject;
    }

    /**
     * Set the user list whom will receive the e-mail notifications
     * 
     * @param aUsersList 
     */
    public void setUsersList(List<String> aUsersList) {
        this.mUsersList = aUsersList;
    }

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

    /**
     * Sending the notification
     * 
     * @param aMessage 
     */
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

    @Override
    public void initialize() throws Exception {
        //Initializing the mail client
        mMailer = new SimpleEmail();
        mMailer.setHostName(mHostName);
        mMailer.setSSL(false);
        mMailer.setSslSmtpPort(mPort);
    }

    @Override
    public void shutdown() throws Exception {
    }

    /**
     * Sends a mail with the given message to the registered recipients 
     * 
     * @param aMessage
     * @throws Exception 
     */
    private void sendMail(String aMessage) throws Exception {
        for (int i = 0; i < mUsersList.size(); i++) {
            mMailer.addTo(mUsersList.get(i));
        }

        mMailer.setFrom(mFrom);
        mMailer.setSubject(mSubject);
        mMailer.setMsg(aMessage);
        mMailer.send();
    }
}
