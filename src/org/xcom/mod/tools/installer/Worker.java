/*
    This file is part of XCOM Mod Manager.

    XCOM Mod Manager is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    XCOM Mod Manager is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with XCOM Mod Manager.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.xcom.mod.tools.installer;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import javax.xml.bind.DatatypeConverter;

import org.xcom.main.shared.Main;
import org.xcom.main.shared.entities.HexEdit;
import org.xcom.main.shared.entities.ResFile;
import org.xcom.mod.gui.streams.Stream;
import org.xcom.mod.tools.xshape.MHash;

/**
 * @author Anthony Surma
 * 
 */
final public class Worker implements Runnable {
	
	final ResFile mod;
	final byte[] buffer;
	final int start;
	final int end;
	final MessageDigest md;
	int id;
	boolean upateUpkPositionOnly = false;
	
	final CountDownLatch mainCountDownLock;
	final CountDownLatch workerCountDownLock;
	final Thread[] workers;
	
	volatile byte[] resultBytes = null;
	volatile int startOffset = -1;
	volatile int endOffset = -1;
	volatile boolean isFound = false;
	private Stream stream;
	
	public Worker(ResFile mod, byte[] buffer, int start, int end, MessageDigest md,
				CountDownLatch mainCountDownLock, CountDownLatch workerCountDownLock,
				Thread[] workers, int id, Boolean upateUpkPositionOnly) {
		
		this.mod = mod;
		this.buffer = buffer;
		this.start = start;
		this.end = end;
		this.md = md;
		this.mainCountDownLock = mainCountDownLock;
		this.workerCountDownLock = workerCountDownLock;
		this.workers = workers;
		this.id = id;
		this.upateUpkPositionOnly = upateUpkPositionOnly;
		
		if (upateUpkPositionOnly) {
			this.stream = Main.MAKE;
		} else {
		this.stream = Main.INSTALL;
		}
	}
	
	@Override
	public void run() {
		
		final int hashDataLength = mod.getSearchHashLength();
		
		final int targetSum = mod.getCheckSum();
		final byte[] searchHash = MHash.hexStringGetBytes(mod.getSearchHash());
		
		String work = "...";
		
		for (int j = start; j <= end; ++j) {
			final int m = j + hashDataLength;
			int k = j;
			int thisSum = 0;
			
			while (k < m) {
				thisSum += buffer[k++] & 0xFF;
			}
			
			if (Thread.interrupted()) {
				return;
			}
			if (thisSum == targetSum) {
				try {
					// inconsequential formula to spread out console output
					if (j % 48 == (id * 1) % 24) {
						print("SEARCHING", (work += "."));
					}
					
					final byte[] bufferSegmt = Arrays.copyOfRange(buffer, j, m);
					final byte[] hash = md.digest(bufferSegmt);
					
					if (Arrays.equals(hash, searchHash)) {
						print("FOUND - CHECKSUM & HASH MATCH");
						
						
						if (!upateUpkPositionOnly) {
							for (final HexEdit c : mod.getChanges()) {
								
								print("BUFFER OFFSET [" + c.getOffset(), "] CHANGED TO [" + c.getData(),
											"]");
								
								if (!mod.getIsSameSize()) {
									resultBytes = MHash
												.hexStringGetBytes(mod.getChanges().get(0).getData());
									
									print("BUFFER OFFSET START [" + j, "] END OFFSET [" + (m), "] WITH ["
												+ resultBytes.length + "] BYTES TO WRITE");
								} else {
									
									bufferSegmt[c.getOffset()] = DatatypeConverter.parseHexBinary(c
												.getData())[0];
									
									print("BUFFER OFFSET [" + c.getOffset(),
												"] CHANGED TO [" + c.getData(), "]");
								}
							}
							
							if (!mod.getIsSameSize()) {
								// Instead of changes replace. Changes will be null in this
								// instance so above code is skipped
								endOffset = m;
								
							} else {
								resultBytes = bufferSegmt;
							}
						}
						
						startOffset = j;
						isFound = true;
						
						for (Thread t : workers) {
							if (t != null && !Thread.currentThread().equals(t)) {
								t.interrupt();
							}
						}
						mainCountDownLock.countDown();
						break;
					}
				} finally {
					md.reset();
				}
			}
		}
		
		workerCountDownLock.countDown();
		try {
			workerCountDownLock.await();
		} catch (InterruptedException ex) {}
		mainCountDownLock.countDown();
	}
	
	private void print(String... strings) {
		Main.print(stream, strings);
	}
}
