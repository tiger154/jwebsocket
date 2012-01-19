package org.jwebsocket.watchdog.api;

/**
 *
 * @author lester
 */
public interface IWatchDogTest extends IMongoDocument {

    /**
     * Getter
     */
    String getImplClass();
    String getDescription();
    String getId();
    String getIdTask();
    Boolean isFatal();
    /*
     * Setter
     */
    void setIdTask(String idTask);
    void setImplClass(String ImplClass);
    void setDescription(String description);
    void setId(String id);
    void setIsFatal(Boolean isFatal);
}
