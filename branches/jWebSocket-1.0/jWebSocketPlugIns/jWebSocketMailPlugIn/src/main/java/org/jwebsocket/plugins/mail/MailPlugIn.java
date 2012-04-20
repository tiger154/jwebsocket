//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket SMTP Plug-In
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//  THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
//  THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
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
package org.jwebsocket.plugins.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author aschulze
 */
public class MailPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	// if namespace changed update client plug-in accordingly!
	private static final String NS_MAIL = JWebSocketServerConstants.NS_BASE + ".plugins.mail";
	private static MailStore mMailStore = new MailStore();
	private static ApplicationContext mBeanFactory;
	private static Settings mSettings;

	public MailPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Mail plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_MAIL);

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for mail plug-in, some features may not be available.");
			} else {
				mBeanFactory = getConfigBeanFactory();
				mSettings = (Settings) mBeanFactory.getBean("settings");
				if (mLog.isInfoEnabled()) {
					mLog.info("Mail plug-in successfully instantiated"
							+ ", SMTP: " + mSettings.getSmtpHost() + ":" + mSettings.getSmtpPort()
							+ ", POP3: " + mSettings.getPop3Host() + ":" + mSettings.getPop3Port()
							+ ".");
				}
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating mail plug-in"));
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			// select from database
			if (lType.equals("sendMail")) {
				sendMail(aConnector, aToken);
			} else if (lType.equals("createMail")) {
				createMail(aConnector, aToken);
			} else if (lType.equals("dropMail")) {
				dropMail(aConnector, aToken);
			} else if (lType.equals("addAttachment")) {
				addAttachment(aConnector, aToken);
			} else if (lType.equals("removeAttachment")) {
				removeAttachment(aConnector, aToken);
			} else if (lType.equals("moveMail")) {
				moveMail(aConnector, aToken);
			}
		}
	}

	private Token sendMail(Token aToken) {
		String lFrom = aToken.getString("from", "[unknown]");
		String lTo = aToken.getString("to");
		String lCC = aToken.getString("cc");
		String lBCC = aToken.getString("bcc");
		String lSubject = aToken.getString("subject");
		String lBody = aToken.getString("body");
		Boolean lIsHTML = aToken.getBoolean("html", false);
		List<String> lAttachedFiles = aToken.getList("attachments");
		String lMsg;

		// instantiate response token
		Token lResponse = TokenFactory.createToken();

		Map lMap = new FastMap();

		if (lFrom != null && lFrom.length() > 0) {
			lMap.put("from", lFrom);
		}
		if (lTo != null && lTo.length() > 0) {
			lMap.put("to", lTo);
		}
		if (lCC != null && lCC.length() > 0) {
			lMap.put("cc", lCC);
		}
		if (lBCC != null && lBCC.length() > 0) {
			lMap.put("bcc", lBCC);
		}
		if (lSubject != null && lSubject.length() > 0) {
			lMap.put("subject", lSubject);
		}
		if (lBody != null && lBody.length() > 0) {
			lMap.put("body", lBody);
		}

		// Create the attachment
		List<EmailAttachment> lEmailAttachments = new FastList<EmailAttachment>();

		if (lAttachedFiles != null) {
			for (int lIdx = 0; lIdx < lAttachedFiles.size(); lIdx++) {
				EmailAttachment lAttachment = new EmailAttachment();
				lAttachment.setPath(lAttachedFiles.get(lIdx));
				lAttachment.setDisposition(EmailAttachment.ATTACHMENT);
				// lAttachment.setDescription( "Picture of John" );
				// lAttachment.setName( "John" );
				lEmailAttachments.add(lAttachment);
			}
		}

		// Create the lEmail message
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending e-mail to " + lTo + " with subject '" + lSubject + "'...");
		}
		try {
			Email lEmail;
			if (lIsHTML) {
				lEmail = new HtmlEmail();
			} else {
				lEmail = new MultiPartEmail();
			}

			lEmail.setHostName(mSettings.getSmtpHost());
			lEmail.setSmtpPort(mSettings.getSmtpPort());
			if (mSettings.getSmtpAuth()) {
				lEmail.setAuthentication(
						mSettings.getSmtpUser(),
						mSettings.getSmtpPassword());
			}
			if (mSettings.getSmtpPop3Before()) {
				lEmail.setPopBeforeSmtp(
						true,
						mSettings.getPop3Host(),
						mSettings.getPop3User(),
						mSettings.getPop3Password());
			}
			if (lFrom != null && lFrom.length() > 0) {
				lEmail.setFrom(lFrom);
			}
			if (lTo != null && lTo.length() > 0) {
				lEmail.addTo(lTo);
			}
			if (lSubject != null && lSubject.length() > 0) {
				lEmail.setSubject(lSubject);
			}

			if (lBody != null && lBody.length() > 0) {
				if (lIsHTML) {
					HtmlEmail lHTML = ((HtmlEmail) lEmail);
					/*
					 * URL lURL = new
					 * URL("http://five-feet-further.com/aschulze/images/portrait_web_kleiner.jpg");
					 * String lCID = ((HtmlEmail )lEmail).embed(lURL, "five feet
					 * further logo");
					 *
					 * //url = new URL(
					 * "http://five-feet-further.com/resources/css/IJX4FWDocu.css"
					 * ); // String css = ((HtmlEmail)lEmail).embed( url, "name
					 * of css" );
					 *
					 * ((HtmlEmail )lEmail).setHtmlMsg( "<html><body>" + "<style
					 * type=\"text/css\">" + "h1 { " + " font-family:arial,
					 * helvetica, sans-serif;" + " font-weight:bold;" + "
					 * font-size:18pt;" + "}" + "</style>" + // "<link
					 * href=\"cid:" + css + "\" type=\"text/css\"
					 * rel=\"stylesheet\">" + "<p><img src=\"cid:" + lCID +
					 * "\"></p>" + "<p><img
					 * src=\"http://five-feet-further.com/aschulze/images/portrait_web_kleiner.jpg\"></p>"
					 * + lItem + "</body></html>");
					 */

					/*
					 * // Now the message body. Multipart mp = new
					 * MimeMultipart();
					 *
					 * BodyPart textPart = new MimeBodyPart(); // sets type to
					 * "text/plain" textPart.setText("Kann Ihr Browser keine
					 * HTML-Mails darstellen?");
					 *
					 * BodyPart pixPart = new MimeBodyPart();
					 * pixPart.setContent(lMsg, "text/html");
					 *
					 * // Collect the Parts into the MultiPart
					 * mp.addBodyPart(textPart); mp.addBodyPart(pixPart);
					 *
					 * // Put the MultiPart into the Message ((HtmlEmail)
					 * lEmail).setContent((MimeMultipart)mp); ((HtmlEmail)
					 * lEmail).buildMimeMessage();
					 *
					 * /*
					 * // ((HtmlEmail) lEmail).setContent(lMsg,
					 * Email.TEXT_HTML);
					 *
					 * // lHeaders.put("Innotrade-Id", "4711-0815"); //
					 * lHTML.setHeaders(lHeaders); // ((HtmlEmail)
					 * lEmail).setCharset("UTF-8"); // ((HtmlEmail)
					 * lEmail).setMsg(lMsg); lMM.setHeader("Innotrade-Id",
					 * "4711-0815");
					 *
					 * // ((HtmlEmail) lEmail).setContent(lTxtMsg,
					 * Email.TEXT_PLAIN);
					 */
					// String lTxtMsg = "Your Email-Client does not support HTML messages.";
					lHTML.setHtmlMsg(lBody);
					// lHTML.setTextMsg(lTxtMsg);
				} else {
					lEmail.setMsg(lBody);
				}
			}

			// add attachment(s), if such
			for (EmailAttachment lAttachment : lEmailAttachments) {
				((MultiPartEmail) lEmail).attach(lAttachment);
			}

			// send the Email
			String lMsgId = lEmail.send();

			if (mLog.isInfoEnabled()) {
				lMsg = "Email successfully sent"
						+ " from " + (lFrom != null ? lFrom : "(no sender)")
						+ " to " + (lTo != null ? lTo : "(no recipient)")
						+ " cc " + (lCC != null ? lCC : "(no recipient)")
						+ ", subject " + (lSubject != null ? "'" + lSubject + "'" : "(no subject)")
						+ ", msgId " + lMsgId;
				mLog.info(lMsg);
			}
			lResponse.setInteger("code", 0);
			lResponse.setString("msg", "ok");
			lResponse.setString("msgId", lMsgId);
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + " (" + lEx.getCause().getClass().getSimpleName() + "): " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}
		return lResponse;
	}

	private Token archiveAttachments(WebSocketConnector aConnector, Token aToken, Token aMail, String aTargetFolder) {
		Token lRes = TokenFactory.createToken();
		Integer lVolumeSize = aToken.getInteger("volumeSize", -1);
		String lArchiveName = aToken.getString("archiveName", "PackagedAttachments{partNo}.zip");
		List<String> lAttachments = aMail.getList("attachments");
		int MIN_VOLUME_SIZE = 16348;
		int BUFFER_SIZE = 65536;
		if (lVolumeSize >= MIN_VOLUME_SIZE
				&& null != lArchiveName
				&& null != lAttachments
				&& lAttachments.size() > 0) {
			// start to create auto splitted package archive

			try {
				// rar process
				List<String> lCmdLine = new FastList<String>();
				// lCmdLine.add(System.getenv("windir") +"\\system32\\"+"tree.com");
				// lCmdLine.add("/A");
				lCmdLine.add(mSettings.getRarPath()); // path to rar
				lCmdLine.add("-y"); // Assume Yes on all queries.
				lCmdLine.add("-m5"); // compression level 0-5.
				lCmdLine.add("-ep1"); // disable path names in archive.
				lCmdLine.add("-v" + lVolumeSize + "b"); // Assume Yes on all queries.
				lCmdLine.add("a"); // add command
				lCmdLine.add(aTargetFolder + "test.rar"); // target archive
				for (String lAttachment : lAttachments) {
					lCmdLine.add(lAttachment); // add files to compress
				}
				if (mLog.isDebugEnabled()) {
					mLog.debug("Executing: " + lCmdLine.toString());
				}

				ProcessBuilder lProcessBuilder = new ProcessBuilder(lCmdLine);
				/*
				 * Map<String, String> lEnvVars = lProcessBuilder.environment();
				 * lProcessBuilder.directory(new File(System.getenv("temp")));
				 * if (mLog.isDebugEnabled()) { mLog.debug("Directory : " +
				 * System.getenv("temp")); }
				 */
				final Process lProcess = lProcessBuilder.start();
				InputStream is = lProcess.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
				if (mLog.isDebugEnabled()) {
					mLog.debug("Rar process finished.");
				}

				List<String> lVolumeIds = new FastList<String>();
				String lMask = "test*.*";
				File lDir = new File(aTargetFolder);
				String[] lFiles = lDir.list(new WildcardFileFilter(lMask));
				if (null != lFiles) {

					Token lEvent = TokenFactory.createToken(NS_MAIL, BaseToken.TT_EVENT);
					lEvent.setString("name", "volumeCreated");

					for (int lIdx = 0; lIdx < lFiles.length; lIdx++) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Creating volume mail for " + lFiles[lIdx]);
						}
						Token lVolMail = TokenFactory.createToken();
						String lVolId = "volume" + (lIdx + 1);
						lVolMail.setString("id", lVolId);
						lVolMail.setString("from", aMail.getString("from"));
						lVolMail.setString("to", aMail.getString("to"));
						lVolMail.setString("cc", aMail.getString("cc"));
						lVolMail.setString("bcc", aMail.getString("bcc"));
						lVolMail.setString("subject", aMail.getString("subject") + " (part " + (lIdx + 1) + ")");
						lVolMail.setString("body", aMail.getString("body"));
						lVolMail.setBoolean("html", aMail.getBoolean("html"));
						List<String> lVolAtt = new FastList<String>();
						lVolAtt.add(aTargetFolder + lFiles[lIdx]);
						lVolMail.setList("attachments", lVolAtt);
						lVolumeIds.add(lVolId);
						mMailStore.storeMail(lVolMail);

						lEvent.setString("id", lVolId);
						// send response to requester
						getServer().sendToken(aConnector, lEvent);

					}
					aMail.setList("volumeIds", lVolumeIds);
					mMailStore.storeMail(aMail);
				} else {
					// TODO: process error!
				}

				// zip process
				/*
				 * String lArchiveAbsolutePath = aTargetFolder + lArchiveName;
				 *
				 * // Reference to the file we will be adding to the zipfile
				 * BufferedInputStream lSource = null;
				 *
				 * // Reference to our zip file FileOutputStream lDest = new
				 * FileOutputStream(lArchiveAbsolutePath);
				 *
				 * // Wrap our destination zipfile with a ZipOutputStream
				 * ZipOutputStream lZipOut = new ZipOutputStream(new
				 * BufferedOutputStream(lDest));
				 *
				 * // Create a byte[] buffer that we will read data from the
				 * source // files into and then transfer it to the zip file
				 * byte[] lBuff = new byte[BUFFER_SIZE];
				 *
				 * // Iterate over all of the files in our list for (String
				 * lAttachment : lAttachments) { String lFilenameInArchive =
				 * FilenameUtils.getName(lAttachment); // Get a
				 * BufferedInputStream that we can use to read the source file
				 * if (mLog.isDebugEnabled()) { mLog.debug("Adding " +
				 * lAttachment + " to " + lFilenameInArchive + "..."); }
				 * System.out.println(); FileInputStream lFileIn = new
				 * FileInputStream(lAttachment); lSource = new
				 * BufferedInputStream(lFileIn, BUFFER_SIZE);
				 *
				 * // Setup the entry in the zip file // here you can specify
				 * the name and folder in the archive ZipEntry lEntry = new
				 * ZipEntry(lFilenameInArchive); lZipOut.putNextEntry(lEntry);
				 *
				 * // Read data from the source file and write it out to the
				 * zip file int lRead; while ((lRead = lSource.read(lBuff, 0,
				 * BUFFER_SIZE)) != -1) { lZipOut.write(lBuff, 0, lRead); }
				 *
				 * // Close the source file lSource.close(); }
				 *
				 * // Close the zip file lZipOut.close();
				 */


				/*
				 * FileInputStream lFIS = new
				 * FileInputStream(lArchiveAbsolutePath); // Read data from the
				 * source file and write it out to the zip file int lRead; int
				 * lPart = 0; lBuff = new byte[lVolumeSize]; while ((lRead =
				 * lFIS.read(lBuff, 0, lVolumeSize)) != -1) { lPart++;
				 * FileOutputStream lFOS = new
				 * FileOutputStream(lArchiveAbsolutePath + ".part" + lPart);
				 * lFOS.write(lBuff, 0, lRead); lFOS.close(); } lFIS.close();
				 */
				lRes.setInteger("code", 0);
			} catch (Exception lEx) {
				lRes.setInteger("code", -1);
				lRes.setString("msg", lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
			}
		}
		return lRes;
	}

	private void sendMail(WebSocketConnector aConnector, Token aToken) {
		String lId = aToken.getString("id");

		String lMsg;

		TokenServer lServer = getServer();
		Token lResponse = createResponse(aToken);

		Token lMailToken = mMailStore.getMail(lId);
		if (null == lMailToken) {
			lMsg = "Send mail: No mail with id '" + lId + "' found.";
			if (mLog.isDebugEnabled()) {
				mLog.debug(lMsg);
			}
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
			// send error response to requester
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		List<String> lVolumeIds = lMailToken.getList("volumeIds");
		if (null != lVolumeIds) {
			for (String lVolumeId : lVolumeIds) {
				Token lVolToken = mMailStore.getMail(lVolumeId);
				if (null != lVolToken) {
					Token lSendMailToken = sendMail(lVolToken);
					Token lEvent = TokenFactory.createToken(NS_MAIL, BaseToken.TT_EVENT);
					lEvent.setString("name", "volumeSend");
					lEvent.setString("id", lVolToken.getString("id"));
					lEvent.setInteger("code", lSendMailToken.getInteger("code", -1));
					lEvent.setString("msg", lSendMailToken.getString("msg", "[message missing]"));
					lEvent.setString("msgId", lSendMailToken.getString("msgId", "[message-id missing]"));
					// send response to requester
					lServer.sendToken(aConnector, lEvent);
				}
				lResponse.setInteger("code", -1);
				// TODO: implement multivolume response
				lResponse.setString("msg", "[multi volume to be implemented]");
				lResponse.setString("msgId", "[multi volume to be implemented]");
				lResponse.setString("id", "[multi volume to be implemented]");
			}
		} else {
			Token lSendMailToken = sendMail(lMailToken);
			lResponse.setInteger("code", lSendMailToken.getInteger("code", -1));
			lResponse.setString("msg", lSendMailToken.getString("msg", "[message missing]"));
			lResponse.setString("msgId", lSendMailToken.getString("msgId", "[message-id missing]"));
			lResponse.setString("id", lId);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	public static String convertEmbeddedImagesToAttachments(String aString) {
		// <img src="data:image/jpeg;base64,
		String lPattern = "<img.*src=\"\\{([A-Za-z0-9_]+)\\}\".*>";
		int lFlags = Pattern.CASE_INSENSITIVE;
		Pattern lRegExpr = Pattern.compile(lPattern, lFlags);
		Matcher lMatcher = lRegExpr.matcher(aString);
		while (lMatcher.find()) {
			String lFoundVal = lMatcher.group(1);
			String lEnvVal = "REPLACED";
			Pattern lSubExpr = Pattern.compile(Pattern.quote(lMatcher.group(0)));
			aString = lSubExpr.matcher(aString).replaceAll(lEnvVal);
		}
		return aString;
	}
	
	private void createMail(WebSocketConnector aConnector, Token aToken) {
		Token lMailToken = TokenFactory.createToken();

		String lFrom = aToken.getString("from", "[unknown]");
		String lTo = aToken.getString("to");
		String lCC = aToken.getString("cc");
		String lBCC = aToken.getString("bcc");
		String lSubject = aToken.getString("subject");
		String lBody = aToken.getString("body");
		Boolean lIsHTML = aToken.getBoolean("html", false);
		Boolean lConvEmbImgToAtt = aToken.getBoolean("convEmbImgToAtt", false);

		if (lConvEmbImgToAtt) {
			lBody = convertEmbeddedImagesToAttachments(lBody);
		}

		// only take over valid fields of the token
		String lId = UUID.randomUUID().toString();
		lId = "myMail";
		lMailToken.setString("id", lId);
		lMailToken.setString("from", lFrom);
		lMailToken.setString("to", lTo);
		lMailToken.setString("cc", lCC);
		lMailToken.setString("bcc", lBCC);
		lMailToken.setString("subject", lSubject);
		lMailToken.setString("body", lBody);
		lMailToken.setBoolean("html", lIsHTML);

		TokenServer lServer = getServer();
		Token lResponse = createResponse(aToken);

		Token lMailStoreToken = mMailStore.storeMail(lMailToken);

		lResponse.setInteger("code", lMailStoreToken.getInteger("code", -1));
		lResponse.setString("msg", lMailStoreToken.getString("msg", "-"));
		lResponse.setString("id", lId);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void dropMail(WebSocketConnector aConnector, Token aToken) {
		String lId = aToken.getString("id");

		TokenServer lServer = getServer();
		Token lResponse = createResponse(aToken);

		Token lMailStoreToken = mMailStore.removeMail(lId);
		lResponse.setInteger("code", lMailStoreToken.getInteger("code", -1));
		lResponse.setString("msg", lMailStoreToken.getString("msg", "-"));


		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void addAttachment(WebSocketConnector aConnector, Token aToken) {
		String lId = aToken.getString("id");
		String lFilename = aToken.getString("filename");
		String lData = aToken.getString("data");
		String lEncoding = aToken.getString("encoding", "base64");
		String lMsg;

		TokenServer lServer = getServer();
		Token lResponse = createResponse(aToken);

		Token lMailStoreToken = mMailStore.getMail(lId);
		if (null == lMailStoreToken) {
			lMsg = "No mail with id '" + lId + "' found.";
			if (mLog.isDebugEnabled()) {
				mLog.debug(lMsg);
			}
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
			// send error response to requester
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		byte[] lBA = null;
		try {
			if ("base64".equals(lEncoding)) {
				int lIdx = lData.indexOf(',');
				if (lIdx >= 0) {
					lData = lData.substring(lIdx + 1);
				}
				lBA = Base64.decodeBase64(lData);
			} else {
				lBA = lData.getBytes("UTF-8");
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " on save: " + lEx.getMessage());
		}

		String lBaseDir;
		String lUsername = getUsername(aConnector);
		lBaseDir = mSettings.getMailRoot();
		if (lUsername != null) {
			lBaseDir = FilenameUtils.getFullPath(
					JWebSocketConfig.expandEnvAndJWebSocketVars(lBaseDir).replace("{username}", lUsername));
		}

		// complete the response token
		String lFullPath = lBaseDir + lFilename;
		File lFile = new File(lFullPath);
		try {
			// prevent two threads at a time writing to the same file
			synchronized (this) {
				// force create folder if not yet exists
				File lDir = new File(FilenameUtils.getFullPath(lFullPath));
				FileUtils.forceMkdir(lDir);
				if (lBA != null) {
					FileUtils.writeByteArrayToFile(lFile, lBA);
				} else {
					FileUtils.writeStringToFile(lFile, lData, "UTF-8");
				}
			}
			List<String> lAttachments = lMailStoreToken.getList("attachments");
			if (null == lAttachments) {
				lAttachments = new FastList<String>();
				lMailStoreToken.setList("attachments", lAttachments);
			}
			lAttachments.add(lFile.getAbsolutePath());

			mMailStore.storeMail(lMailStoreToken);

			Token lSplitToken = archiveAttachments(aConnector, aToken, lMailStoreToken, lBaseDir);

		} catch (Exception lEx) {
			lResponse.setInteger("code", -1);
			lMsg = lEx.getClass().getSimpleName() + " on save: " + lEx.getMessage();
			lResponse.setString("msg", lMsg);
			mLog.error(lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void removeAttachment(WebSocketConnector aConnector, Token aToken) {
	}

	private void moveMail(WebSocketConnector aConnector, Token aToken) {
	}

	private void getMail(WebSocketConnector aConnector, Token aToken) {
	}

	private void getUserMails(WebSocketConnector aConnector, Token aToken) {
	}
}
