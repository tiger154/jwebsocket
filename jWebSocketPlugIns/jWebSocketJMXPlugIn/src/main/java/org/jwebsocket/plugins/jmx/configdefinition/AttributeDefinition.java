// ---------------------------------------------------------------------------
// jWebSocket - JMXPlugIn v1.0
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
// THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
// THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.plugins.jmx.configdefinition;

import javax.management.modelmbean.ModelMBeanAttributeInfo;

/**
 * Class that allows to define the attributes of the plugins or classes to 
 * export and their metadata.
 * 
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class AttributeDefinition extends FeatureDefinition {

	private Class mType;
	private Boolean mIsReadable = false;
	private Boolean mIsWritable = false;
	private Boolean mIsBoolean = false;

	/**
	 * The class default constructor
	 */
	public AttributeDefinition() {
	}

	/**
	 *
	 * @param aType
	 * @param aName
	 * @param aDescription
	 * @param aIsReadable
	 * @param aIsWritable
	 * @param aIsBoolean
	 */
	public AttributeDefinition(Class aType, String aName, String aDescription, 
			Boolean aIsReadable, Boolean aIsWritable, Boolean aIsBoolean) {
		super(aName, aDescription);
		this.mType = aType;
		this.mIsReadable = aIsReadable;
		this.mIsWritable = aIsWritable;
		this.mIsBoolean = aIsBoolean;
	}

	/**
	 *
	 * @return Class
	 */
	public Class getType() {
		if (this.mType != null) {
			return this.mType;
		} else {
			throw new IllegalArgumentException("The attribute type must not be "
					+ "null.");
		}
	}

	/**
	 *
	 * @param aType
	 */
	public void setType(Class aType) {
		if (aType != null) {
			this.mType = aType;
		} else {
			throw new IllegalArgumentException("The attribute type must not be "
					+ "null.");
		}
	}

	/**
	 *
	 * @return Boolean
	 */
	public Boolean isReadable() {
		return mIsReadable;
	}

	/**
	 *
	 * @param aReadable
	 */
	public void setReadable(Boolean aReadable) {
		this.mIsReadable = aReadable;
	}

	/**
	 *
	 * @return Boolean
	 */
	public Boolean isWritable() {
		return mIsWritable;
	}

	/**
	 *
	 * @param aWritable
	 */
	public void setWritable(Boolean aWritable) {
		this.mIsWritable = aWritable;
	}

	/**
	 *
	 * @return Boolean
	 */
	public Boolean isBoolean() {
		return mIsBoolean;
	}

	/**
	 *
	 * @param aIsBoolean
	 */
	public void setIsBoolean(Boolean aIsBoolean) {
		this.mIsBoolean = aIsBoolean;
	}

	/**
	 * Create the metadata of the attribute definition.
	 * 
	 * @return ModelMBeanAttributeInfo
	 */
	public ModelMBeanAttributeInfo createMBeanAttributeInfo() {
		if (this.mType != null) {
			return new ModelMBeanAttributeInfo(super.getName(), mType.getName(),
					super.getDescription(), mIsReadable, mIsWritable, mIsBoolean);
		} else {
			throw new IllegalArgumentException("The attribute type must not be null.");
		}
	}
}
