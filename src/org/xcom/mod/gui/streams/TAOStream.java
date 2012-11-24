package org.xcom.mod.gui.streams;

import java.io.IOException;

import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;

// Singleton class for diverting the System.out stream to a text area. No longer used but keeping code.
public final class TAOStream extends OutputStream {

	private static final TAOStream INSTANCE = new TAOStream();
	private static JTextArea outWriter;
	private static final PrintStream OUT;
	private static boolean TAINTED = false;

	private final StringBuilder sb = new StringBuilder();

	static {
		OUT = System.out;
		System.setOut(new PrintStream(new TAOStream()));
	}

	public TAOStream() {
	}

	/** Gets the output stream. */
	public static TAOStream getStream(JTextArea textArea) {
		outWriter = textArea;
		return INSTANCE;
	}
		

	/**
	 * Gets the functioning console output.
	 * 
	 * @see java.lang.System.out
	 */
	public static PrintStream getOldSystemOut() {
		return OUT;
	}

	/** Determines if output has occurred. */
	public static boolean isTainted() {
		return TAINTED;
	}
	
	/** Determines if output has occurred. */
	public static JTextArea getOutWriter() {
		return outWriter;
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
