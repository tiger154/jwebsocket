//	---------------------------------------------------------------------------
//	jWebSocket - ValidatorFilter (Community Edition, CE)
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
package org.jwebsocket.eventmodel.filter.validator;

import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.core.EventModel;
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
 * @author Rolando Santamaria Maso
 */
public class ValidatorFilter extends EventModelFilter {

	private static Logger mLog = Logging.getLogger(ValidatorFilter.class);
	private TypesMap mTypes;

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
				if (mLog.isDebugEnabled()) {
					for (Iterator<FieldError> lIt = lErrors.getFieldErrors().iterator(); lIt.hasNext();) {
						FieldError lError = lIt.next();
						mLog.debug("Detected argument error: [" + lError.getField() + ": " + lError.getCode() + "]");
					}
				}
				String lFields = "";
				for (FieldError lField : lErrors.getFieldErrors()) {
					lFields += lField.getField() + ",";
				}
				throw new ValidatorException("Invalid incoming arguments: "
						+ lFields.substring(0, lFields.length() - 1));
			}
		}
	}

	/**
	 * {@inheritDoc }
	 *
	 * @param aResponseEvent
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
				if (mLog.isDebugEnabled()) {
					for (Iterator<FieldError> lIt = lErrors.getFieldErrors().iterator(); lIt.hasNext();) {
						FieldError lError = lIt.next();
						mLog.debug("Detected argument error: [" + lError.getField() + ": " + lError.getCode() + "]");
					}
				}
				String lFields = "";
				for (Object lField : lErrors.getFieldErrors()) {
					lFields += ((FieldError) lField).getField() + ",";
				}
				throw new ValidatorException("Invalid outgoing arguments: "
						+ lFields.substring(0, lFields.length() - 1));
			}
		}
	}

	/**
	 * Validate an argument
	 *
	 * @param aArg The argument to validate
	 * @param aEvent The event that contain the argument
	 * @param aErrors
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
	 * @return <tt>TRUE</tt> if the filter require to validate the response too,
	 * <tt>FALSE</tt> otherwise
	 */
	public boolean isValidateResponse() {
		return getEm().getEnv().equals(EventModel.DEV_ENV);
	}
}
