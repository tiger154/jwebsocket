package org.jwebsocket.util;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
/*
 * @author vbarzanacres
 */
public class Console extends OutputStream {

	private JTextArea mOutput;

	public Console(JTextArea output) {
		this.mOutput = output;
	}

	/*
	 * This method is called when you invoke System.out.print or println
	 */
	@Override
	public synchronized void write(int aByte) throws IOException {
		mOutput.append(String.valueOf((char) aByte));
		try {
			mOutput.setCaretPosition(mOutput.getLineStartOffset(mOutput.getLineCount()-1));
		} catch (BadLocationException aException) {
			mOutput.append(aException.getMessage());
		}
	}
}
