//	---------------------------------------------------------------------------
//	jWebSocket - ExtJS Plugin
//	Copyright (c) 2011 Innotrade GmbH, jWebSocket.org, Alexander Schulze,
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.extjs;

import java.util.LinkedList;
import javolution.util.FastList;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Osvaldo Aguilar Lauzurique, Alexander Rojas Hernandez
 */
public class ExtJSDemoPlugin extends TokenPlugIn {

    private static Users u = new Users();

    public ExtJSDemoPlugin(PluginConfiguration aConfiguration) {
        super(aConfiguration);
        setNamespace(aConfiguration.getNamespace());

    }

    @Override
    public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
        if (aToken.getNS().equals(getNamespace())) {
            
            if (aToken.getType().equals("create"))
                processCreate(aResponse, aConnector, aToken);

            else if (aToken.getType().equals("update"))
                processUpdate(aResponse, aConnector, aToken);

            else if (aToken.getType().equals("read"))
                processRead(aResponse, aConnector, aToken);

            else if (aToken.getType().equals("destroy"))
                processDestroy(aResponse, aConnector, aToken);

            else if (aToken.getType().equals("getAllUsers"))
                getAllUsers(aResponse, aConnector, aToken);
        }
    }

    private void processCreate(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {

        String name = aToken.getString("name");
        String email = aToken.getString("email");

        int lenght = u.getUsers().size();
        Token result = createResponse(aToken);

        if ((name != null) && (email != null)) {
            if ((name.trim().equals("")) || (email.trim().equals(""))) {
                getServer().sendErrorToken(aConnector, aToken, -1, "Campo nombre no dado");
            } else {
                UserDef userToAdd = new UserDef(u.getCount(), name, email);
                try {
                    u.add(userToAdd);
                    result.setInteger("code", 0);
                    result.setString("message", "User created correctly");

                } catch (Exception ex) {

                    result.setString("message", ex.getMessage());
                    result.setInteger("code", -1);
                }

                //REACHING DATA FOR SHOWING TO THE USER
                FastList<Token> data = new FastList<Token>();
                Token ta = TokenFactory.createToken();
                ta.setInteger("id", userToAdd.getId());
                ta.setString("name", name);
                ta.setString("email", email);
                data.add(ta);

                //SETTING THE DATA LIST TO THE OUTGOING TOKEN
                result.setList("data", data);

                //SENDING THE TOKEN
                getServer().sendToken(aConnector, result);
            }
        } else {
            getServer().sendErrorToken(aConnector, aToken, -1, "Campo nombre no dado");
        }

    }

    private void processUpdate(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken)
    {
              
                String name = aToken.getString("name");
                String email = aToken.getString("email");
                Integer id = aToken.getInteger("id");

                Token result = createResponse(aToken);

                UserDef user = u.getUser(id);

                if (user != null) {
                    user.setEmail(email);
                    user.setName(name);


                    result.setInteger("code", 0);
                    result.setString("message", "User with id: " + id + " updated correctly");

                    //REACHING DATA FOR SHOWING TO THE USER
                    FastList<Token> data = new FastList<Token>();
                    Token ta = TokenFactory.createToken();
                    ta.setString("name", user.getName());
                    ta.setString("email", user.getEmail());
                    ta.setInteger("id", user.getId());
                    data.add(ta);


                    result.setList("data", data);
                } else {
                    result.setInteger("code", -1);
                    result.setString("message", "An error has occurred.  could not update the user with id: " + id);
                }
                getServer().sendToken(aConnector, result);
    }

    private void processRead(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken)
    {
                Integer id = Integer.parseInt(aToken.getString("id"));
                UserDef user = u.getUser(id);
                Token result = createResponse(aToken);

                FastList<Token> data = new FastList<Token>();

                if (user != null) {

                    result.setInteger("code", 0);
                    result.setString("message", "User found with id: " + id);

                    Token ta = TokenFactory.createToken();
                    ta.setString("name", user.getName());
                    ta.setString("email", user.getEmail());
                    ta.setInteger("id", user.getId());
                    data.add(ta);
                } else {
                    result.setInteger("code", -1);
                    result.setString("message", "there is no customer with id: " + id);
                }

                result.setList("data", data);

                getServer().sendToken(aConnector, result);
    }

    private void processDestroy(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken)
    {
                Integer id = aToken.getInteger("id");
                Token result = createResponse(aToken);

                FastList<Token> data = new FastList<Token>();

                if (u.deleteUser(id)) {
                    result.setInteger("code", 0);
                    result.setString("message", "delete success customer with id: " + id);
                    result.setList("data", data);
                } else {
                    result.setInteger("code", -1);
                    result.setString("message", "An error has occurred.  could not delete the user with id: " + id);
                }

                getServer().sendToken(aConnector, result);
    }


    private void getAllUsers(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken)
    {
         LinkedList<UserDef> users = u.getUsers();
                FastList<Token> data = new FastList<Token>();

                Token result = createResponse(aToken);

                if (users.size() > 0) {

                    for (UserDef ud : users) {

                        Token ta = TokenFactory.createToken();
                        ta.setString("name", ud.getName());
                        ta.setString("email", ud.getEmail());
                        ta.setInteger("id", ud.getId());
                        data.add(ta);
                    }

                    result.setInteger("code", 0);
                    result.setList("data", data);

                } else {
                    result.setInteger("code", -1);
                    result.setString("message", "has not created any customer");
                }

                getServer().sendToken(aConnector, result);
    }


}
