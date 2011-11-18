package org.jwebsocket.client.plugins.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.jwebsocket.plugins.rpc.CommonRpcPlugin;
import org.jwebsocket.plugins.rpc.MethodMatcher;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

public class RPCPlugin {
	private static boolean annotationAllowed = true ;
	public static void setAnnotationAllowed (boolean aAnnotationAllowedValue){
		annotationAllowed = aAnnotationAllowedValue;
	}
	private static Map<String, Map<String, List<MethodRPCCallable>>> mListOfMethod = new FastMap<String, Map<String, List<MethodRPCCallable>>>();
	
	
	/**
	 * Add a Method to the list of rpc granted. rrpc authorization is default: false.
	 * @param aMethod
	 */
	public static void addRrpcMethod (Method aMethod) {
		addRrpcMethod (aMethod, false);
	}
	
	/**
	 * Add a Method to the list of rrpc granted method. 
	 * If aRrpcCallable is true, allow other client to access to this method using a rrpc.
	 * If false, only server rpc will be allowed
	 * @param aMethod
	 * @param aRrpcCallable 
	 */
	public synchronized static void addRrpcMethod (Method aMethod, boolean aRrpcCallable) {
		String lClassName = aMethod.getDeclaringClass().getName();
		if (!mListOfMethod.containsKey(lClassName)) {
			mListOfMethod.put(lClassName, new FastMap<String, List<MethodRPCCallable>>());
		}
		Map<String, List<MethodRPCCallable>> lMap = mListOfMethod.get(lClassName) ;
		if (!lMap.containsKey(aMethod.getName())) {
			lMap.put(aMethod.getName(), new FastList<MethodRPCCallable>());
		}
		List<MethodRPCCallable> lList = lMap.get(aMethod.getName());
		MethodRPCCallable lMethodRPCCallable = new MethodRPCCallable(aMethod, aRrpcCallable);
		if(!lList.contains(lMethodRPCCallable)) {
			lList.add(lMethodRPCCallable);
		}
	}
	/**
	 * Process a rrpc call.
	 * TODO: doesn't send back any answer. Do smthg with lMsg & lResponseToken.
	 * @param aClassName
	 * @param aMethodName
	 * @param aArgs
	 */
	public static Token processRrpc(String aClassName, String aMethodName, List aArgs, String aSourceId) {
		boolean lRrpcFromServer = CommonRpcPlugin.SERVER_ID.equals(aSourceId);
		Token lResponseToken = null; 
		String lMsg = "";
		if (aClassName != null && aMethodName != null) {
			if (mListOfMethod.containsKey(aClassName)) {
				if (mListOfMethod.get(aClassName).containsKey(aMethodName))
				return call(aClassName, aMethodName, aArgs, lRrpcFromServer);
			} 
			
			if (annotationAllowed){
				//We try to load the method and check if it has the annotation
				try {
					Class lClass = Class.forName(aClassName);
					Method[] lMethods = lClass.getMethods();
					for (Method lMethod : lMethods) {
						if (lMethod.getName().equals(aMethodName) && lMethod.isAnnotationPresent(RPCCallable.class)) {
							addRrpcMethod(lMethod, lMethod.getAnnotation(RPCCallable.class).C2CAuthorized());
							//Add the method to the list to grant a faster access.
							MethodMatcher lMethodMatcher = new MethodMatcher(lMethod);
							if (lMethodMatcher.isMethodMatchingAgainstParameter(aArgs)) {
								lResponseToken = call(aClassName, aMethodName, aArgs, lRrpcFromServer);
							}
						}
					}
					if (lResponseToken != null) {
						return lResponseToken;
					}
				} catch (ClassNotFoundException ex) {
		      lMsg = "ClassNotFoundException calling '" + aMethodName + "' for class " + aClassName + ": " + ex.getMessage();
				}
			}
			else {
				lMsg = "Class not found in the list (annotation are not allowed) calling '" + aMethodName + "' for class " + aClassName + ": " ;
			}
		}
		if (lMsg.equals("")) {
			lMsg = "ClassName or Method name is probably null calling '" + aMethodName + "' for class " + aClassName + ": " ;
		}
		lResponseToken = TokenFactory.createToken(CommonRpcPlugin.NS_RPC_DEFAULT, CommonRpcPlugin.RPC_TYPE);		
		lResponseToken.setString("msg", lMsg);
		return lResponseToken;
	}
	
	private static Token call (String aClassName, String aMethodName, List aArgs, boolean lRrpcFromServer) {
		Token lResponseToken = TokenFactory.createToken(CommonRpcPlugin.NS_RPC_DEFAULT, CommonRpcPlugin.RPC_TYPE);
		String lMsg = "";
		List<MethodRPCCallable> lListOfMethod = mListOfMethod.get(aClassName).get(aMethodName);
    try {
			for (MethodRPCCallable lMethod : lListOfMethod) {
				MethodMatcher lMethodMatcher = new MethodMatcher(lMethod.getMethod());
		    //If lArg is not null, means the method match
		    if (lMethodMatcher.isMethodMatchingAgainstParameter(aArgs)) {
		    	if (lRrpcFromServer || (!lRrpcFromServer && lMethod.isRrpcCallable())) {
			      //We cast the intance to the correct class.
							Object lObj = lMethod.getMethod().invoke(null, lMethodMatcher.getMethodParameters());
							lResponseToken.setValidated("result", lObj);
							return lResponseToken;
		    	} else {
		    		lMsg = "Only the server can invoke this method. Right isn't granted for a C2C call (only S2C).";
		    		break;
		    	}
		    }
		  }
		} catch (IllegalArgumentException ex) {
      lMsg = "IllegalAccessException calling '" + aMethodName + "' for class " + aClassName + ": " + ex.getMessage();
		} catch (IllegalAccessException ex) {
      lMsg = "IllegalAccessException calling '" + aMethodName + "' for class " + aClassName + ": " + ex.getMessage();
		} catch (InvocationTargetException ex) {
      lMsg = "InvocationTargetException calling '" + aMethodName + "' for class " + aClassName + ": " + ex.getMessage();
		} catch (Exception ex) {
      lMsg = "InvocationTargetException calling '" + aMethodName + "' for class " + aClassName + ": " + ex.getMessage();
		}
    lResponseToken.setInteger("code", -1);
    lResponseToken.setString("msg", lMsg);
    return lResponseToken;
	}
	
	private static class MethodRPCCallable {
		private boolean rrpcCallable;
		private Method method;
		MethodRPCCallable (Method aMethod, boolean aRrpcCallable) {
			method = aMethod ;
			rrpcCallable = aRrpcCallable ;
		}
		public boolean isRrpcCallable() {
			return rrpcCallable;
		}
		public Method getMethod() {
			return method;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((method == null) ? 0 : method.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MethodRPCCallable other = (MethodRPCCallable) obj;
			if (method == null) {
				if (other.method != null)
					return false;
			} else if (!method.equals(other.method))
				return false;
			return true;
		}
	}
}
