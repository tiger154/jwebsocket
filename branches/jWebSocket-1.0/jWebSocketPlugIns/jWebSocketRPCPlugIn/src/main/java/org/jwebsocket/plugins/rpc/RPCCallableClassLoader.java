//	---------------------------------------------------------------------------
//	jWebSocket - RPCCallableClassLoader
//	Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.rpc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javolution.util.FastMap;

/**
 * 1 instance of RPCCallableClassLoader is used for each class.
 * RPCCallableClassLoader contains a RPCCallable instance of the class it represents and
 * the list of methods with associated rights (the relaction is stored in a MethodRightLink object)
 * @author Quentin Ambard
 */
public class RPCCallableClassLoader {

	private Class mRpcCallableClass;
	private RPCCallable mRpcCallableInstance;
	private Map<String, List<MethodRightLink>> mMethods = new FastMap<String, List<MethodRightLink>>();

	public RPCCallableClassLoader(Class aRpcCallableClass, RPCCallable aRpcCallableInstance) {
		this.mRpcCallableClass = aRpcCallableClass;
		this.mRpcCallableInstance = aRpcCallableInstance;
	}

	/**
	 * Add a tuple (method, right) to the list method of the RPCCallableClassLoader
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

	public Class getRpcCallableClass() {
		return mRpcCallableClass;
	}

	public RPCCallable getRpcCallableInstanceGenerator() {
		return mRpcCallableInstance;
	}

	public boolean hasMethod(String aMethod) {
		return mMethods.containsKey(aMethod);
	}

	public List<MethodRightLink> getMethods(String aMethodName) {
		return mMethods.get(aMethodName);
	}

	/**
	 * make a link between a Right and a Method
	 * @author Kiou
	 */
	public class MethodRightLink {

		private String mRightId;
		private Method mMethod;

		public MethodRightLink(Method aMethod, String aRightId) {
			mMethod = aMethod;
			mRightId = aRightId;
		}

		public String getRightId() {
			return mRightId;
		}

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
