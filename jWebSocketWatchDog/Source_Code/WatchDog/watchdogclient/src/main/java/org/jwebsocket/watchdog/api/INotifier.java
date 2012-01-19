package org.jwebsocket.watchdog.api;

import java.util.List;
import org.jwebsocket.api.IInitializable;

/**
 *
 * @author lester
 */
public interface INotifier extends IInitializable {

    /**
     * Get the ID of the notifier
     */
    public String getId();

    /*
     * set the ID of the notifier
     */
    public void setId(String id);
    
    /*
     * get the Decription of the notifier
     */
    public String getDescription();
    
    /*
     * set the Decription of the notifier
     */
    public void setDescription(String description);
    
    /*
     * List of users whom will receive the notifications
     */
    List<String> getTo();
    
    /*
     * set the users whom will receive the notifications
     */
    void setTo(List<String> to);
    
    /*
     * sending the message
     */
    public void notify(String message);
}
