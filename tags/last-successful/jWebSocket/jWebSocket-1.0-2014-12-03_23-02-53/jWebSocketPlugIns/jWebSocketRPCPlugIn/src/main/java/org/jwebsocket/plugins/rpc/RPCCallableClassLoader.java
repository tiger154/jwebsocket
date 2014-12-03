//	---------------------------------------------------------------------------
//	jWebSocket - RPCCallableClassLoader (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.rpc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javolution.util.FastMap;

/**
 * 1 instance of RPCCallableClassLoader is used for each class.
 * RPCCallableClassLoader contains a RPCCallable instance of the class it
 * represents and the list of methods with associated rights (the relaction is
 * stored in a MethodRightLink object)
 *
 * @author Quentin Ambard
 */
public class RPCCallableClassLoader {

	private final Class mRpcCallableClass;
	private final RPCCallable mRpcCallableInstance;
	private final Map<String, List<MethodRightLink>> mMethods = new FastMap<String, List<MethodRightLink>>();

	/**
	 *
	 * @param aRpcCallableClass
	 * @param aRpcCallableInstance
	 */
	public RPCCallableClassLoader(Class aRpcCallableClass, RPCCallable aRpcCallableInstance) {
		this.mRpcCallableClass = aRpcCallableClass;
		this.mRpcCallableInstance = aRpcCallableInstance;
	}

	/**
	 * Add a tuple (method, right) to the list method of the
	 * RPCCallableClassLoader
	 *
	 * @param aMethodName
	 * @param aMethod
	 * @param aRightId
	 */
	public void addMethod(String aMethodName, Method aMethod, String aRightId) {
		if (!mMethods.containsKey(aMethodName)) {
			mMethods.put(aMethodName, new ArrayList<MethodRightLink>());
		}
		mMethods.get(aMethodName).add(new MethodRightLink(aMethod, aRightId));
	}

	/**
	 *
	 * @return
	 */
	public Class getRpcCallableClass() {
		return mRpcCallableClass;
	}

	/**
	 *
	 * @return
	 */
	public RPCCallable getRpcCallableInstanceGenerator() {
		return mRpcCallableInstance;
	}

	/**
	 *
	 * @param aMethod
	 * @return
	 */
	public boolean hasMethod(String aMethod) {
		return mMethods.containsKey(aMethod);
	}

	/**
	 *
	 * @param aMethodName
	 * @return
	 */
	public List<MethodRightLink> getMethods(String aMethodName) {
		return mMethods.get(aMethodName);
	}

	/**
	 * make a link between a Right and a Method
	 *
	 * @author Kiou
	 */
	public class MethodRightLink {

		private final String mRightId;
		private final Method mMethod;

		/**
		 *
		 * @param aMethod
		 * @param aRightId
		 */
		public MethodRightLink(Method aMethod, String aRightId) {
			mMethod = aMethod;
			mRightId = aRightId;
		}

		/**
		 *
		 * @return
		 */
		public String getRightId() {
			return mRightId;
		}

		/**
		 *
		 * @return
		 */
		public Method getMethod() {
			return mMethod;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((mRightId == null) ? 0 : mRightId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			MethodRightLink other = (MethodRightLink) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (mRightId == null) {
				if (other.mRightId != null) {
					return false;
				}
			} else if (!mRightId.equals(other.mRightId)) {
				return false;
			}
			return true;
		}

		private RPCCallableClassLoader getOuterType() {
			return RPCCallableClassLoader.this;
		}
	}
}
