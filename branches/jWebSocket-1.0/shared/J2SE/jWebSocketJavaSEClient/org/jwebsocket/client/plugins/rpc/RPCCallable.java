package org.jwebsocket.client.plugins.rpc;

import java.lang.annotation.*;

/**
 * 
 * Allow a method to be called by the server. If C2CAuthorized is true, also allow a client to perform a built-in C2C rrpc.
 * @author Quentin Ambard
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RPCCallable {
	boolean C2CAuthorized() default false;
}
