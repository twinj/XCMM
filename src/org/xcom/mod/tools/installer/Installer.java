/*
 * This file is part of XCOM Mod Manager.
 * 
 * XCOM Mod Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * XCOM Mod Manager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with XCOM Mod Manager. If not, see <http://www.gnu.org/licenses/>.
 */

package org.xcom.mod.tools.installer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.xml.bind.JAXBException;

import org.xcom.main.shared.Main;
import org.xcom.main.shared.XmlSaveException;
import org.xcom.main.shared.entities.InstallLog;
import org.xcom.main.shared.entities.ResFile;
import org.xcom.main.shared.entities.XMod;
import org.xcom.mod.tools.shared.ByteScannerChannel;
import org.xcom.mod.tools.shared.ExportFileAccessException;
import org.xcom.mod.tools.shared.UpkFileAccessException;
import org.xcom.mod.tools.shared.UpkFileNotDecompressedException;
import org.xcom.mod.tools.shared.UpkResourceNotFoundException;
import org.xcom.mod.tools.xshape.MHash;

/**
 * @author Anthony Surma
 * 
 */
final public class Installer extends Main {
	
	final static String COOKED = "\\XComGame\\CookedPCConsole\\";
	final static String DIFF_DATA_TAG = "DiffData";
	final static String SEARCH_HASH_TAG = "SearchHash";
	final static String DATA_SUM_TAG = "DataSum";
	private final static int numWorkers = (NUM_CPU > 1 ? NUM_CPU - 1 : NUM_CPU);
	
	private XMod installPackage;
	private List<Path> upkFiles;
	private File modFile;
	
	public Installer(File modFile) {
		super();
		this.modFile = modFile;
	}
	
	@Override
	public void run() {
		
		printActionMessage("INSTALL");
		try {
			installPackage = (XMod) u.unmarshal(modFile);
			printXml(installPackage, "MOD FILE FOR INSTALLATION");
		} catch (Exception e) {
			try {
				throw new ExportFileAccessException();
			} catch (ExportFileAccessException ex) {
				ERROR = Error.INS_EXPORT_EXTRACTION;
				ex.printStackTrace(System.err);
				setDone(true);
				return;
			}
		}
		
		// Get upk files for each resource
		List<ResFile> files = installPackage.getResFiles();
		try {
			upkFiles = findResourceUpkFile(files);
			
			List<ResFile> out = null;
			
			out = makeUPKChanges(files, new Vector<Path>(upkFiles));
			
			installPackage.setResFiles(out);
			// TODO MAY HAVE TO INCLUDE SOME OF FULL INSTALL CHECK SO CAN
			// ROLLBACK
			installPackage.setIsInstalled(true);
			InstallLog log = new InstallLog(installPackage.getName(), installPackage
						.getAuthor(), installPackage.getDescription(), null, null);
			saveXml(log);
			printXml(log);
			
		} catch (UpkFileNotDecompressedException e) {
			ERROR = Error.INS_UPK_FILE_COMPRESSED;
			ret = e.getFiles();
			e.printStackTrace(System.err);
		} catch (UpkFileNotFoundException e) {
			ERROR = Error.INS_UPK_FILE_NF;
			e.printStackTrace(System.err);
		} catch (UpkFileAccessException e) {
			ERROR = Error.INS_UPK_FILE_NA;
			e.printStackTrace(System.err);
		} catch (UpkResourceNotFoundException e) {
			ERROR = Error.INS_UPK_RES_NF;
			e.printStackTrace(System.err);
		} catch (SearchInterruptedException e) {
			ERROR = Error.INS_FATAL;
			e.printStackTrace(System.err);
		} catch (XmlSaveException e) {
			ERROR = Error.XML_SAVE_ERROR;
			e.printStackTrace(System.err);
		}
		setDone(true);
	}
	/**
	 * Run on console.
	 * 
	 * @throws UpkFileNotDecompressedException
	 * @throws UpkFileAccessException
	 * @throws UpkFileNotFoundException
	 * @throws ExportFileAccessException
	 * @throws UpkResourceNotFoundException
	 * @throws SearchInterruptedException
	 * @throws XmlSaveException
	 * 
	 * @throws JAXBException
	 */
	public void runc() throws UpkFileNotFoundException, UpkFileAccessException,
				UpkFileNotDecompressedException, ExportFileAccessException,
				SearchInterruptedException, UpkResourceNotFoundException, XmlSaveException {
		printActionMessage("INSTALL");
		
		try {
			installPackage = (XMod) u.unmarshal(modFile);
			printXml(installPackage, "MOD EXPORT FILE FOR INSTALLATION");
		} catch (JAXBException e) {
			throw new ExportFileAccessException("JAXBException");
		}
		
		// Get upk files for each resource
		List<ResFile> files = installPackage.getResFiles();
		
		upkFiles = findResourceUpkFile(files);
		
		List<ResFile> out = makeUPKChanges(files, new Vector<Path>(upkFiles));
		
		// TODO MAY HAVE TO INCLUDE SOME OF FULL INSTALL CHECK SO CAN
		// ROLLBACK
		
		installPackage.setResFiles(out);
		installPackage.setIsInstalled(true);
		InstallLog log = new InstallLog(installPackage.getName(), installPackage.getAuthor(),
					installPackage.getDescription(), null, null);
		saveXml(log);
		printXml(log);
	}
	
	/**
	 * Gets all of the upk file owners for a resource.
	 * 
	 * @param files
	 * @return
	 * @throws UpkFileNotDecompressedException
	 * @throws UpkFileNotFoundException
	 */
	static List<Path> findResourceUpkFile(List<ResFile> files)
				throws UpkFileNotDecompressedException, UpkFileNotFoundException,
				UpkFileAccessException {
		
		List<Path> upkFiles = new Vector<Path>(files.size());
		List<Path> uncFiles = new Vector<Path>(files.size());
		
		// Get all changed upk files from the resources
		for (ResFile f : files) {
			
			Path path = Paths.get(config.getXcomPath(), COOKED, f.getUpkFilename());
			
			if (Files.notExists(path)) {
				print("CANNOT FIND UPK [" + path.getFileName(), "]");
				throw new UpkFileNotFoundException();
			}
			try {
				if (!Files.isWritable(path)) {
					throw new IOException();
				} else if (!isDecompressed(path)) {
					if (!uncFiles.contains(path)) {
						uncFiles.add(path);
						print("UPK FOUND - NOT DECOMPRESSED [" + path.getFileName(), "]");
					}
					continue;
				}
			} catch (IOException e) {
				String msg = "CANNOT ACCESS UPK [" + path.getFileName() + "]";
				print(msg, "");
				throw new UpkFileAccessException("IOException: " + msg);
			}
			print("UPK FOUND - IS DECOMPRESSED [" + path.getFileName(), "]");
			upkFiles.add(path);
		}
		if (!uncFiles.isEmpty()) throw new UpkFileNotDecompressedException(uncFiles);
		else return upkFiles;
	}
	/**
	 * Initiates worker threads to make Upk file changes.
	 * 
	 * @param files
	 * @throws UpkResourceNotFoundException
	 * @throws UpkFileAccessException
	 * @throws SearchInterruptedException
	 * 
	 * @return
	 */
	static List<ResFile> makeUPKChanges(List<ResFile> files, Vector<Path> upkFiles)
				throws UpkResourceNotFoundException, UpkFileAccessException,
				SearchInterruptedException {
		
		print("INSTALLING RESOURCE CHANGES", "");
		Worker[] workers = new Worker[numWorkers];
		Thread[] threads = new Thread[numWorkers];
		
		int i = 0;
		for (ResFile f : files) {
			Path upkFile = upkFiles.get(i);
			Boolean found = false;
			Boolean started = false;
			
			try (ByteScannerChannel sc = new ByteScannerChannel(upkFile);
						FileChannel ch = sc.getChannel();
						FileLock lock = ch.tryLock()) {
				started = true;
				ByteBuffer buffer = ByteBuffer.wrap(new byte[(int) (ch.size())]);
				
				print(INSTALL, CONSOLE_SEPARATOR, "");
				print(INSTALL, "SEARCHING [" + upkFile.getFileName(), "] FOR RESOURCE [", f
							.getResName(), "]");
				print("WITH CHECKSUM [" + f.getCheckSum(), "]");
				print("WITH HASH [", MHash.toPrintString(f.getSearchHash()), "]");
				
				ch.read(buffer);
				
				try {
					workers = doSearch(numWorkers, workers, threads, i, buffer, f);
				} catch (InterruptedException e) {
					throw new SearchInterruptedException("InterruptedException");
				}
				
				// GET WROKER RESULTS
				for (Worker w : workers) {
					
					if (w.isInstalled) {
						if (w.resultBytes != null) {
							found = true;
							
							if (w.endOffset == -1) {
								print("WRITING HEX EDITS [" + f.getResName(), "]");								
								ch.position(w.resultInt);
								ch.write(ByteBuffer.wrap(w.resultBytes));
							
							} else if (w.endOffset != -1) {
								print("REPLACING RESOURCE [" + f.getResName(), "]");						
								byte[] eof = new byte[(int) (ch.size() - w.endOffset)];
								sc.seek(w.endOffset);
								sc.readFully(eof);
								sc.seek(w.resultInt);
								sc.write(w.resultBytes);
								sc.write(eof);
							}			
						}
						editedUpks.add(upkFile);
						files.get(i).setIsInstalled(true);
					}
				}
				// Most likely an access issue as exists is checked earlier
				// When file is open in UE Explorer it detects as accessible but
				// does not throw any access exceptions. Even with lock.
				
			} catch (IOException e) {
				throw new UpkFileAccessException("IOException");
			} finally {
				if (!started) {
					throw new UpkFileAccessException("IOException,started");
				}
				if (!found) {
					// TODO MAY HAVE TO INCLUDE SOME OF FULL INSTALL CHECK SO
					// CAN ROLLBACK
					print("RESOURCE NOT FOUND [" + f.getResName(),
								"] PLEASE CHECK YOUR CONFIG SETTINGS.", "");
					
					print("\n",
								"THE SYSTEM CURRENTLY DOES NOT UNDO CHANGES PLEASE DO SO MANUALLY AND REPORT ERROR.\n",
								CONSOLE_SEPARATOR);
					throw new UpkResourceNotFoundException();
				}
			}
			i++;
		}
		return files;
	}
	/**
	 * Creates searchers and searcher threads. Does the search.
	 * 
	 * @param threadsNum
	 * @param workers
	 * @param threads
	 * @param i
	 * @param buffer
	 * @param ins
	 * @param progress
	 * @param f
	 * @return
	 * @throws InterruptedException
	 */
	static Worker[] doSearch(int threadsNum, Worker[] workers, Thread[] threads, int i,
				ByteBuffer buffer, ResFile f) throws InterruptedException {
		
		final CountDownLatch mainCDLatch = new CountDownLatch(1);
		final CountDownLatch workerCDLatch = new CountDownLatch(threadsNum);
		final int length = buffer.capacity() / threadsNum;
		
		for (int j = 0; j < threadsNum; ++j) {
			
			int start = j * length;
			int end = start + (length - 1);
			
			workers[j] = new Worker(f, buffer.array(), start, end, md, mainCDLatch,
						workerCDLatch, threads, j + 1);
			
			threads[j] = new Thread(workers[j]);
		}
		for (Thread t : threads) {
			t.start();
		}
		mainCDLatch.await();
		return workers;
	}
	
	private static void print(String... strings) {
		print(INSTALL, strings);
	}
	
	public XMod getInstallPackage() {
		return installPackage;
	}
	
	public List<Path> getUpkFiles() {
		return upkFiles;
	}
	
	/**
	 * Gui only. Determines the progress to be allocated for each part of work to
	 * be done.
	 * 
	 * @param mConfig
	 * @return
	 */
	static float[] calculateWorkProgress(List<ResFile> files, Vector<Path> upkFiles,
				int threadsNum) {
		
		int i = 0;
		int size = files.size();
		float[] passesForRes = new float[size];
		
		for (ResFile f : files) {
			passesForRes[i++] = f.getChanges().size();
		}
		
		// size twice as plusProgress called twice in file loops
		float progressStart = (99 - size - size);
		float[] progress = new float[size];
		
		float sum = 0;
		i = 0;
		// Group percentage
		for (Path p : upkFiles) {
			int length = (int) p.toFile().length();
			sum += (length * passesForRes[i]);
			progress[i] = (length * passesForRes[i]);
			i++;
		}
		i = 0;
		for (float p : progress) {
			float n = (p / sum) * progressStart;
			progress[i] = n;
		}
		return progress;
	}
}
