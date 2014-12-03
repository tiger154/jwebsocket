package org.jwebsocket.util;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

/*
 *
 * @author Victor Antonio Barzana Crespo 
 * @author Alexander Schulze
 */
public class OutputStreamConsole extends OutputStream {

        private final JTextArea mOutput;

        /**
         *
         * @param output
         */
        public OutputStreamConsole(JTextArea output) {
                this.mOutput = output;
        }

        /*
         * This method is called when you invoke System.out.print or println
         */
        @Override
        public synchronized void write(int aByte) throws IOException {
                mOutput.append(String.valueOf((char) aByte));
                try {
                        mOutput.setCaretPosition(mOutput.getLineStartOffset(mOutput.getLineCount() - 1));
                } catch (BadLocationException aException) {
                        mOutput.append(aException.getMessage());
                }
        }
}
