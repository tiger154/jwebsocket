package org.jwebsocket.plugins;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.jwebsocket.plugins.rpc.TestServerMethodMatcher;

@RunWith(value=Suite.class)
@SuiteClasses(value = {
		TestServerMethodMatcher.class
	})
public class SuiteRpc {

}
