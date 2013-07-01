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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import org.jwebsocket.watchdog.forms.Main;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public class GlobsNotifier{
    Main form;
    public GlobsNotifier(final Main form) {
        //you declare the object icon type
        final TrayIcon SystemTryIcon;
        //verifies that the SystemTray is supported
        if (SystemTray.isSupported()) {
            //you get a static instance of SystemTray class
            SystemTray tray = SystemTray.getSystemTray();
            //this is the image of the icon
            Image imagenIcono = Toolkit.getDefaultToolkit().getImage("conf/icon.png");
            //this listener allows us to capture any event
            //done with the mouse on the icon           
            MouseListener mouseListener = new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    System.out.println("System Tray Icon- Mouse clicked!");
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    System.out.println("System Tray Icon - Mouse entered!");
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    System.out.println("System Tray Icon - Mouse exited!");
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    System.out.println("System Tray Icon - Mouse pressed!");
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    System.out.println("System Tray Icon - Mouse released!");
                }
            };


            //this listener is associated with a contextual menu item
            //that appears when you do right click on the icon
            ActionListener lExitListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
            };
            //menu that appears when you do right click
            PopupMenu popup = new PopupMenu();
            MenuItem item = new MenuItem("Exit");

            item.addActionListener(lExitListener);
            popup.add(item);

            //TrayIcon object started
            SystemTryIcon = new TrayIcon(imagenIcono,
                    "jWebSocket WatchDog Client", popup);
            //this type of listener capture the double click on the icon
            ActionListener accionMostrarMensaje = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    
                    SystemTryIcon.displayMessage("Task Result",
                            "Successfuly!",
                            TrayIcon.MessageType.INFO);
                    form.toFront();
                }
            };

            SystemTryIcon.setImageAutoSize(true);
            SystemTryIcon.addActionListener(accionMostrarMensaje);
            SystemTryIcon.addMouseListener(mouseListener);

            //an exception must be catch in case of failre 
            //trying add the icon to the try
            try {
                tray.add(SystemTryIcon);
            } catch (AWTException e) {
                System.err.println("Unable to add icon to the System Tray");
            }
        } else {
            System.err.println("Your system does not support the System Tray");
        }
    }

}
