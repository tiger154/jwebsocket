package org.jwebsocket.watchdog;

import forms.Main;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) {
        try {
           /* String name = "WatchDog";
            Registry registry = LocateRegistry.getRegistry(1234);
            IWatchDogService watchdog = (IWatchDogService) registry.lookup(name);
            System.out.print(watchdog.start());
            System.out.print(">>Connected succesfully");*/

            /*ApplicationContext ctx = new FileSystemXmlApplicationContext("conf/config.xml");
            MailNotifier mailer = ((MailNotifier)ctx.getBean("mailnotifier"));
            mailer.notify("Testing!!!!");*/
           
           new Main().show();

        } catch (Exception e) {
            System.err.println("WatchDog exception:");
            e.printStackTrace();
        }
    }
}
