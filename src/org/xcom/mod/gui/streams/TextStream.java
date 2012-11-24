package org.xcom.mod.gui.streams;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

public class TextStream extends OutputStream {
	
	private JTextArea outWriter = null;
	private boolean TAINTED = false;
	private boolean useOut = true;
	private static java.io.PrintStream OUT = System.out;
	private java.io.PrintStream THIS;

	private final StringBuilder sb = new StringBuilder();


	public TextStream() {
	}
	
	public TextStream(JTextArea textArea) {
		this.outWriter = textArea;
		useOut = false;
	}
	
	/** Determines if output has occurred. */
	public boolean isTainted() {
		return TAINTED;
	}
	
	/** Determines if output has occurred. */
	public JTextArea getOutWriter() {
		return outWriter;
	}
	
	public java.io.PrintStream getStream() {
		return (outWriter == null ? OUT : THIS);
	}
	
	public boolean useOut() {
		return useOut;
	}

	/** Write output to the Text Area. */
	@Override
	public void write(int b) throws IOException {
		if (b == '\r') {
			return;
		}
		if (b == '\n') {
			outWriter.append(sb.toString());
			sb.setLength(0);
		}		
		sb.append( (char) b);	
		TAINTED = true;
	}

}
