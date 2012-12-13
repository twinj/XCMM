package org.xcom.mod.tools.shared;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public class ByteScannerChannel extends RandomAccessFile
			implements
				Closeable,
				AutoCloseable {
	
	public ByteScannerChannel(Path file) throws FileNotFoundException {
		super(file.toFile(), "rw");
	}
	
	public enum Eol {
		CR,
		NL,
		CRNL;
	}
	
	/**
	 * Find a new line by the specified type only.
	 * 
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public final long readEol(Eol type) throws IOException {
		final int EOF = -1;
		int c = -1;
		boolean eol = false;
		
		switch (type) {
			case CRNL :
				while (!eol) {
					switch (c = read()) {
						case 0x0D :
							long cur = getFilePointer();
							if ((c = read()) == 0x0A) eol = true;
							else if (c == 0x0D) seek(cur);
							break;
						case EOF :
							eol = true;
							break;
					}
				}
				break;
			case NL :
				while (!eol) {
					switch (c = read()) {
						case 0x0A :
							eol = true;
							break;
						case EOF :
							eol = true;
							break;
					}
				}
				break;
			case CR :
				while (!eol) {
					switch (c = read()) {
						case 0x0D :
							eol = true;
							break;
						case EOF :
							eol = true;
							break;
					}
				}
				break;
		}
		if (c == EOF) return EOF;
		else return getFilePointer();
	}
	
	public final long findEol(Eol type, long seekPosition) throws IOException {
		this.seek(seekPosition);
		return readEol(type);
	}
	
	/**
	 * Support only Eol type CRLN for now.
	 */
	public byte readByteLine() {
		return (Byte) null;
	}
	

	/**
	 * Returns position if found. Does not reset buffer position.
	 * 
	 * @param buffer
	 * @param pattern
	 * @throws IOException
	 */
	public final long findBytes(ByteBuffer buffer, byte[] pattern) throws IOException {
		
		byte[] bytes = buffer.array();
		int limit = bytes.length;
		int pSize = pattern.length;
		long ret = -1;
		
		int start = buffer.position();
		
		int [] table = new int[256];

		int i, j;
		
		for (i = 0; i < 256; i++) {
			table[i] = pSize;			
		}
		for (j = 0; j <= pSize - 2; j ++ ) {
			i = pattern[j];
			table[i] = pSize - 1 - j;	
		}

		i = start + pSize - 1;
		
		while (i <= limit - 1) {
			int k = 0;
			
			while (k <= pSize - 1 && pattern[pSize-1-k] == ((int) bytes[i-k] & 0xFF)) k++;
								
			if (k == pSize) {
				ret = i - pSize + 1;
				break;
			
			} else {				
				i = i + table[(int)bytes[i] & 0xFF];																			
			}				
		}
		
		//buffer.position((int) (ret + pSize));
		return ret;
	}
}