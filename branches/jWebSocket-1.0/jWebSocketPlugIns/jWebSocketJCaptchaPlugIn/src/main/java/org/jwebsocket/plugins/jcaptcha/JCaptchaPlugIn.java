//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket SMS Plug-In
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.jcaptcha;

import com.octo.captcha.service.CaptchaServiceException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

public class JCaptchaPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(JCaptchaPlugIn.class);
	private String lImageType = null;

	public JCaptchaPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(aConfiguration.getNamespace());
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if ( aToken.getNS().equals(getNamespace())) {
			if ("getcaptcha".equals(aToken.getType())) {
				
				lImageType = aToken.getString("imagetype");
				lImageType = lImageType == null ? "png" : lImageType.trim().toLowerCase();
				
				if (!lImageType.equalsIgnoreCase("png") && !lImageType.equalsIgnoreCase("jpg")
						&& !lImageType.equalsIgnoreCase("jpeg")) {
					lImageType = "png";
				}
				
				generateCaptcha(aToken, aConnector);
			}
			if (aToken.getType().equals("validate")) {
				Token response = createResponse(aToken);
				if (validateCaptcha(aConnector.getSession().getSessionId(), aToken.getString("inputChars"))) {
					response.setString("msg", "Correct");
				} else {
					response.setString("msg", "Wrong");
					response.setInteger("code", -1);
				}
				getServer().sendToken(aConnector, response);
			}
		}
	}

	public void generateCaptcha(Token aToken, WebSocketConnector aConnector) {
		ByteArrayOutputStream imgOutputStream = new ByteArrayOutputStream();
		byte[] captchaBytes = null;
		Token response = TokenFactory.createToken(getNamespace(), "getcaptcha");
		try {
			// Session ID is used to identify the particular captcha.
			String captchaId = aConnector.getSession().getSessionId();

			if (mLog.isDebugEnabled()) {
				mLog.debug("-------------------------------------------------------------");
				mLog.debug("generating captcha for id: " + captchaId);
				mLog.debug("-------------------------------------------------------------");
			}
			// Generate the captcha image.
			//BufferedImage challengeImage = MyCaptchaService.getInstance().getImageChallengeForID(captchaId , aToken.getString("locale"));
			BufferedImage challengeImage = MyCaptchaService.getInstance().getImageChallengeForID(captchaId);

			ImageIO.write(challengeImage, lImageType, imgOutputStream);

			captchaBytes = imgOutputStream.toByteArray();

		} catch (CaptchaServiceException cse) {
			System.out.println("CaptchaServiceException - " + cse.getMessage());
			response.setString("msg", "Problem generating captcha image.");
			getServer().sendToken(aConnector, response);
			return;

		} catch (IOException ioe) {
			System.out.println("IOException - " + ioe.getMessage());
			response.setString("msg", "Problem generating captcha image.");
			getServer().sendToken(aConnector, response);
			return;
		}

		// Write the image to the client.
		response.setString("image", Tools.base64Encode(captchaBytes));
		getServer().sendToken(aConnector, response);
	}

	private boolean validateCaptcha(String captchaId, String inputChars) {
		boolean bValidated = false;
		try {
			bValidated = MyCaptchaService.getInstance().validateResponseForID(captchaId, inputChars);
		} catch (CaptchaServiceException cse) {
		}
		return bValidated;
	}
}
