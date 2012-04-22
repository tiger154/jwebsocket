//  ---------------------------------------------------------------------------
//  jWebSocket - ValidatorFilter
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.filter.validator;

import java.util.Set;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.exception.ValidatorException;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.logging.Logging;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

/**
 *
 * @author kyberneees
 */
public class ValidatorFilter extends EventModelFilter {

	private static Logger mLog = Logging.getLogger(ValidatorFilter.class);
	private TypesMap mTypes;
	private boolean mValidateResponse = true;

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void beforeCall(WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		C2SEventDefinition lDef = getEm().getEventFactory().getEventDefinitions().
				getDefinition(aEvent.getId());

		Set<Argument> lInArgs = lDef.getIncomingArgsValidation();
		if (lInArgs.size() > 0) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Validating incoming arguments for '" + aEvent.getId() + "' event ...");
			}

			//Incoming event args validation
			MapBindingResult lErrors = new MapBindingResult(aEvent.getArgs().getMap(), "request.errors");

			//Calling the global validator
			if (lDef.getValidator() != null && lDef.getValidator().supports(aEvent.getClass())) {
				lDef.getValidator().validate(aEvent, lErrors);
			}

			//Validating by arguments
			for (Argument lArg : lInArgs) {
				validateArg(lArg, aEvent, lErrors);
			}
			if (lErrors.hasErrors()) {
				String lFields = "";
				for (FieldError lField : lErrors.getFieldErrors()) {
					lFields += lField.getField() + ",";
				}
				throw new ValidatorException("Invalid incoming arguments: " + lFields);
			}
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void afterCall(WebSocketConnector aConnector, C2SResponseEvent aResponseEvent) throws Exception {
		C2SEventDefinition lDef = getEm().getEventFactory().getEventDefinitions().
				getDefinition(aResponseEvent.getId());

		if (lDef.isResponseRequired()) {
			//Adding owner connector in the response if checked
			if (lDef.isResponseToOwnerConnector()) {
				aResponseEvent.getTo().add(aConnector.getId());
			}

			//At least 1 connector is needed for delivery
			if (aResponseEvent.getTo().isEmpty()) {
				throw new ValidatorException("A 'WebSocketConnector' set with > 0 size is required for delivery the response!");
			}

			if (!isValidateResponse()) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Omitting validation for '" + aResponseEvent.getId() + "' outgoing arguments ...");
				}
				return;
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("Validating outgoing arguments for '" + aResponseEvent.getId() + "' event ...");
			}

			if (aResponseEvent.getCode() != 0) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Validation aborted. The response state is NOT OK!");
				}
				return;
			}

			//Response event args validation
			MapBindingResult lErrors = new MapBindingResult(aResponseEvent.getArgs().getMap(), "response.errors");

			//Calling the global validator
			if (lDef.getValidator() != null && lDef.getValidator().supports(aResponseEvent.getClass())) {
				lDef.getValidator().validate(aResponseEvent, lErrors);
			}

			//Validating by arguments
			for (Argument lArg : lDef.getOutgoingArgsValidation()) {
				validateArg(lArg, aResponseEvent, lErrors);
			}
			if (lErrors.hasErrors()) {
				String lFields = "";
				for (FieldError lField : lErrors.getFieldErrors()) {
					lFields += lField.getField() + ",";
				}
				throw new ValidatorException("Invalid outgoing arguments: " + lFields);
			}
		}
	}

	/**
	 * Validate an argument
	 *
	 * @param aArg The argument to validate
	 * @param aEvent The event that contain the argument
	 * @param errors The errors messages container
	 * @throws Exception
	 */
	public void validateArg(Argument aArg, Event aEvent, Errors aErrors) throws Exception {
		//Argument validation
		if (!aEvent.getArgs().getMap().containsKey(aArg.getName())) {
			if (!aArg.isOptional()) {
				throw new ValidatorException("The argument: '" + aArg.getName() + "' is required!");
			}
		} else {
			try {
				//Supporting JavaScript parseFloat function issue 
				//when parsing values like 1.0, 2.0 ...
				if (aArg.getType().equals("double") && aEvent.getArgs().getObject(aArg.getName()) instanceof Integer) {
					aEvent.getArgs().getMap().put(aArg.getName(),
							Double.parseDouble(aEvent.getArgs().getObject(aArg.getName()).toString()));
				}
				mTypes.swapType(aArg.getType()).cast(aEvent.getArgs().getObject(aArg.getName()));
			} catch (Exception ex) {
				throw new ValidatorException("Argument: '" + aArg.getName() + "' has invalid type. Required type is: '" + aArg.getType().toString() + "'!");
			}
		}

		//Hydrating the argument with the value
		aArg.setValue(aEvent.getArgs().getObject(aArg.getName()));

		//Spring validation mechanism support
		if (null != aArg.getValidator()) {
			if (aArg.getValidator().supports(mTypes.swapType(aArg.getType()))) {
				aArg.getValidator().validate(aArg, aErrors);
			}
		}
	}

	/**
	 * @return The abstract and java types table
	 */
	public TypesMap getTypes() {
		return mTypes;
	}

	/**
	 * @param aTypes The abstract and java types table to set
	 */
	public void setTypes(TypesMap aTypes) {
		this.mTypes = aTypes;
	}

	/**
	 * @return <tt>TRUE</tt> if the filter is set to validate the response too,
	 * <tt>FALSE</tt> otherwise
	 */
	public boolean isValidateResponse() {
		return mValidateResponse;
	}

	/**
	 * @param aValidateResponse <tt>TRUE</tt> if the filter will validate the
	 * response too, <tt>FALSE</tt> otherwise
	 */
	public void setValidateResponse(boolean aValidateResponse) {
		this.mValidateResponse = aValidateResponse;
	}
}
