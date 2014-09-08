//	---------------------------------------------------------------------------
//	jWebSocket - TTS Plug-in TTS SpeakAloud Provider (Community Edition, CE)
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
package org.jwebsocket.plugins.tts;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Alexander Schulze
 */
public class SpeakAloudProvider implements ITTSProvider {

	static final Logger mLog = Logger.getLogger(SpeakAloudProvider.class);

	private static final String TEXT_FILE = "speak.txt";
	private String mExePath;
	private String mTextPath;

	public SpeakAloudProvider() {
	}

	public String getExePath() {
		return mExePath;
	}

	public void setExePath(String aExePath) {
		mExePath = aExePath;
	}

	public String getTextPath() {
		return mTextPath;
	}

	public void setTextPath(String aTextPath) {
		// is only working under Windows!
		mTextPath = FilenameUtils.normalizeNoEndSeparator(aTextPath) + "\\";
	}

	@Override
	public byte[] generateAudioFromString(String aText, String aGender,
			String aSpeaker, String aFormat) {
		synchronized (this) {
			byte[] lResult = null;
			Process lProcess = null;
			try {
				String lUUID = "speak"; // UUID.randomUUID().toString();
				String lTextFN = mTextPath + lUUID + ".txt";

				mLog.debug("Executing '" + mExePath + " " + lTextFN + "'...");

				// write text from client into text file of server harddisk
				File lTextFile = new File(lTextFN);
				FileUtils.writeStringToFile(lTextFile, aText, "Cp1252");

				// call conversion tool with appropriate arguments
				Runtime lRT = Runtime.getRuntime();
				// pass the text file to be converted.
				String[] lCmd = {mExePath, lTextFN};
				lProcess = lRT.exec(lCmd);

				InputStream lIS = lProcess.getInputStream();
				InputStreamReader lISR = new InputStreamReader(lIS);
				BufferedReader lBR = new BufferedReader(lISR);
				String lLine;
				while ((lLine = lBR.readLine()) != null) {
//						System.out.println(lLine);
				}
				// wait until process has performed completely
				if (lProcess.waitFor() != 0) {
					mLog.error("Converter exited with value " + lProcess.exitValue());
				} else {
					mLog.info("Audiostream successfully generated!");
				}
				String lAudioFN = mTextPath + lUUID + ".MP3";
				File lAudioFile = new File(lAudioFN);
				lResult = FileUtils.readFileToByteArray(lAudioFile);

				FileUtils.deleteQuietly(lTextFile);
				FileUtils.deleteQuietly(lAudioFile);

			} catch (Exception lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "performing TTS conversion"));
			} finally {
				if (null != lProcess) {
					lProcess.destroy();
				}
			}
			return lResult;
		}
	}
}
