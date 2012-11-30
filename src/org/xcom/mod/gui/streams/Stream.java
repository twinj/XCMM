package org.xcom.mod.gui.streams;

import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;

public class Stream extends PrintStream {

	private String delegate;

	public Stream(String delegate, OutputStream out) {
		super(out);
		this.delegate = delegate;
	}

	public Stream(Stream out) {
		super(out);
	}

	public String getDelegate() {
		return delegate;
	}

	public static Stream getStream(String delegate, JTextArea textArea) {
		return new Stream(delegate, new TextStream(textArea));
	}

	public static Stream getStream(String delegate) {
		return new Stream(delegate, System.out);
	}
	
	public static Stream getErrorStream(String delegate) {
		return new Stream(delegate, System.err);
	}
}
