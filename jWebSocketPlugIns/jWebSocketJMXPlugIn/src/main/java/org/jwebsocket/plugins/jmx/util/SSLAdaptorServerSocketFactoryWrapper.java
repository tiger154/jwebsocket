/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.jmx.util;

import java.net.URI;
import mx4j.tools.adaptor.ssl.SSLAdaptorServerSocketFactory;
import org.jwebsocket.util.Tools;

/**
 *
 * @author aschulze
 */
public class SSLAdaptorServerSocketFactoryWrapper extends SSLAdaptorServerSocketFactory {

	@Override
	public void setKeyStoreName(String aName) {
		String lName;
		lName = "file:/" + Tools.expandEnvVarsAndProps(aName);
		super.setKeyStoreName(aName);
	}
}
