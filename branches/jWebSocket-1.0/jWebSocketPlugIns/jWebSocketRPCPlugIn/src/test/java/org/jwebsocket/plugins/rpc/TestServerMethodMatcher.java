package org.jwebsocket.plugins.rpc;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javolution.util.FastList;
import java.util.Arrays;
import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.plugins.rpc.util.ServerMethodMatcher;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;


@RunWith(value=Parameterized.class)
public class TestServerMethodMatcher {
	private WebSocketConnector lWebSocketConnector ;
	private MethodParametersTuple mMethodParametersTuple ;
	private String methodCalled ;
	public TestServerMethodMatcher(MethodParametersTuple aMethodParametersTuple) {
		mMethodParametersTuple = aMethodParametersTuple ;
	}
	@Parameters
	public static Collection getTestParameters () {	   
		List args ;
		MethodParametersTuple test1 = new MethodParametersTuple("test1", null, null, true) ;
		args = new FastList();	
			args.add("aaa");
		MethodParametersTuple test2 = new MethodParametersTuple("test1", new Class[] {String.class}, args, true) ;
		
		args = new FastList();	
			args.add("aaa");  
				List<String> list = new FastList<String>() ; 
				list.add("test");
			args.add(list);
		MethodParametersTuple test3 = new MethodParametersTuple("test1", new Class[] {String.class, List.class}, args, true) ;
		
		args = new FastList();
			//args.add(EasyMock.createMock(WebSocketConnector.class));  
				List<List<String>> list2 = new FastList<List<String>>() ; 
				List<String> subList = new FastList<String>();
					subList.add("aaa");
					subList.add("ooo");
				list2.add(subList);
				list2.add(subList);
			args.add(list2);
			args.add(TokenFactory.createToken());
		MethodParametersTuple test4 = new MethodParametersTuple("test3", new Class[] {WebSocketConnector.class, List.class, Token.class}, args, true) ;
		Object[][] data = new Object[][] { { test1 }, { test2 }, { test3 } , { test4 } };
	  return Arrays.asList(data);
	}
	@Before
	public void init () {
		lWebSocketConnector = EasyMock.createMock(WebSocketConnector.class);
		methodCalled = null ;
	}
	
	@Test
	public void testServerMethodMatcher () {
		Method lMethod = null;
		try {
			lMethod = TestServerMethodMatcher.class.getMethod(mMethodParametersTuple.getMethodName(), mMethodParametersTuple.getMethodArgs());
		} catch (SecurityException e) {
			Assert.fail("SecurityException for method "+mMethodParametersTuple.getMethodName()+" when testing the ServerMethodMatcher");
		} catch (NoSuchMethodException e) {
			Assert.fail(mMethodParametersTuple.getMethodName()+" method can't be found when testing the ServerMethodMatcher "+ e);
		}
		ServerMethodMatcher lServerMethodMatcher = new ServerMethodMatcher(lMethod, lWebSocketConnector);
		boolean isMethodMatchingAgainstParameter = lServerMethodMatcher.isMethodMatchingAgainstParameter(mMethodParametersTuple.getArgs());
		Assert.assertEquals("method test 1 should match against null parameters", mMethodParametersTuple.getExceptedResult(), isMethodMatchingAgainstParameter);
		//Assert.assertEquals("not the correct method called", methodCalled, mMethodParametersTuple.getMethodName());
	}
	
	public void test1 () {
		methodCalled = "test1";
	}
	public void test1 (String aString) {
		methodCalled = "test1";
	}
	public void test1 (String aString, List<String> aList) {
		methodCalled = "test1";
	}
	public void test2 (List<List<String>> aList) {
		methodCalled = "test2";
	}
	public void test2 (List<List<String>> aList, Token aToken) {
		methodCalled = "test2";
	}
	public void test3 (WebSocketConnector aWebSocketConnector, List<List<String>> aList, Token aToken) {
		methodCalled = "test3";
	}
	
	/**
	 * Test case: store a Method+parameter+an excepted result after calling the serverMethodMather.
	 * @author Quentin Ambard
	 *
	 */
	public static class MethodParametersTuple {
		private String mMethodName;
		private List mArgs;
		private boolean mExceptedResult;
		private Class[] mMethodArgs;
		MethodParametersTuple (String aMethodName, Class[] aMethodArgs, List aArgs, boolean aExceptedResult) {
			mMethodName = aMethodName;
			mArgs = aArgs ;
			mExceptedResult = aExceptedResult;
			mMethodArgs = aMethodArgs ;
		}
		public String getMethodName() {
			return mMethodName;
		}
		public List getArgs() {
			return mArgs;
		}
		public boolean getExceptedResult() {
			return mExceptedResult;
		}		
		public Class[] getMethodArgs() {
			return mMethodArgs;
		}
	}
}
