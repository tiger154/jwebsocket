//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket JCaptcha Plug-in
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

/**
 * 
 * @author mayra, vbarzana, aschulze
 */
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
import org.jwebsocket.util.Tools;

public class JCaptchaPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	private String mImgType = null;

	public JCaptchaPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Captcha plug-in...");
		}
		setNamespace(aConfiguration.getNamespace());
		
		if (mLog.isInfoEnabled()) {
			mLog.info("Captcha plug-in successfully instantiated.");
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		
		if (aToken.getNS().equals(getNamespace())) {
			if ("getcaptcha".equals(aToken.getType())) {

				mImgType = aToken.getString("imagetype");
				mImgType =
						mImgType == null
						? "png"
						: mImgType.trim().toLowerCase();

				if (!mImgType.equalsIgnoreCase("png")
						&& !mImgType.equalsIgnoreCase("jpg")
						&& !mImgType.equalsIgnoreCase("jpeg")) {
					mImgType = "png";
				}

				generateCaptcha(aToken, aConnector);

			} else if ("validate".equals(aToken.getType())) {
				Token lResponse = createResponse(aToken);
				if (validateCaptcha(aConnector.getSession().getSessionId(), aToken.getString("inputChars"))) {
					lResponse.setString("msg", "Correct");
				} else {
					lResponse.setString("msg", "Wrong");
					lResponse.setInteger("code", -1);
				}
				getServer().sendToken(aConnector, lResponse);
			}
		}
	}

	public void generateCaptcha(Token aToken, WebSocketConnector aConnector) {
		ByteArrayOutputStream lImgOutputStream = new ByteArrayOutputStream();
		byte[] lCaptchaBytes;
		Token lResponse = createResponse(aToken);
		try {
			// Session ID is used to identify the particular captcha.
			String lCaptchaId = aConnector.getSession().getSessionId();

			if (mLog.isDebugEnabled()) {
				mLog.debug("generating captcha for id: " + lCaptchaId);
			}
			// Generate the captcha image.
			// BufferedImage challengeImage = JWebSocketCaptchaService.getInstance().getImageChallengeForID(captchaId , aToken.getString("locale"));
			BufferedImage lChallengeImage = JWebSocketCaptchaService.getInstance().getImageChallengeForID(lCaptchaId);

			ImageIO.write(lChallengeImage, mImgType, lImgOutputStream);


			lCaptchaBytes = lImgOutputStream.toByteArray();
			// Write the image to the client.
			lResponse.setString("image", Tools.base64Encode(lCaptchaBytes));
			getServer().sendToken(aConnector, lResponse);

		} catch (IOException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "Error generating captcha!"));
		}
	}

	private boolean validateCaptcha(String captchaId, String inputChars) {
		boolean bValidated = false;
		try {
			bValidated = JWebSocketCaptchaService.getInstance().validateResponseForID(captchaId, inputChars);
		} catch (CaptchaServiceException lCSE) {
			mLog.error(Logging.getSimpleExceptionMessage(lCSE, "validating captcha"));
		}
		return bValidated;
	}
}