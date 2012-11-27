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

import org.xcom.mod.Main;
import org.xcom.mod.gui.workers.RunInBackground.SyncProgress;
import org.xcom.mod.pojos.HexEdit;
import org.xcom.mod.pojos.ResFile;
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
	float progress;
	@SuppressWarnings("rawtypes")
	private SyncProgress sync;
	
	// @SuppressWarnings("VolatileArrayField")
	final CountDownLatch mainCountDownLock;
	final CountDownLatch workerCountDownLock;
	final Thread[] workers;
	
	volatile byte[] resultBytes = null;
	volatile int resultInt = -1;
	volatile boolean isInstalled = false;
	
	@SuppressWarnings("rawtypes")
	public Worker(ResFile mod, byte[] buffer, int start, int end,
			MessageDigest md, CountDownLatch mainCountDownLock,
			CountDownLatch workerCountDownLock, Thread[] workers, int id,
			SyncProgress sync, float f) {
		
		this.mod = mod;
		this.buffer = buffer;
		this.start = start;
		this.end = end;
		this.md = md;
		this.mainCountDownLock = mainCountDownLock;
		this.workerCountDownLock = workerCountDownLock;
		this.workers = workers;
		this.id = id;
		this.sync = sync;
		this.progress = f;
	}
	
	@Override
	public void run() {
		
		final int hashDataLength = mod.getSearchHashLength();
		final int end = this.end - hashDataLength;
		
		final long targetSum = mod.getByteSum();
		final byte[] searchHash = MHash.hexStringToBytes(mod.getSearchHash());
		
		String work = "...";
		
		float progressPlus = buffer.length / workers.length / progress;
		
		for (int j = start; j <= end; ++j) {
			final int m = j + hashDataLength;
			int k = j;
			int thisSum = 0;
			
			while (k < m) {
				thisSum += buffer[k++] & 0xFF;
			}
			if (sync != null) {
				// check progress
				if (j % (int)progressPlus == progressPlus - 1) {
					sync.plusProgress(1);
					progress--;
				}
			}
			
			if (Thread.interrupted()) {
				if (sync != null) {
					sync.plusProgress((int) progress);
				}
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
						print("FOUND - BYTE SUM & HASH MATCH");
						
						for (final HexEdit c : mod.getChanges()) {
							print("BUFFER OFFSET [" + c.getOffset(),
									"] CHANGED TO [" + c.getData(), "]");
							
							bufferSegmt[c.getOffset()] = DatatypeConverter.parseHexBinary(c
									.getData())[0];
							
							if (sync != null) {
								sync.plusProgress(1);
							}
						}
						
						resultBytes = bufferSegmt;
						resultInt = j;
						isInstalled = true;
						
						for (Thread t : workers) {
							if (!Thread.currentThread().equals(t)) {
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
	
	private static void print(String... strings) {
		Main.print(Main.INSTALL, strings);
	}
}
