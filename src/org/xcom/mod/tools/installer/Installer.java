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
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import org.xcom.mod.Main;
import org.xcom.mod.exceptions.XmlSaveException;
import org.xcom.mod.gui.XCMGUI;
import org.xcom.mod.gui.workers.DecompressInBackGround;
import org.xcom.mod.gui.workers.RunInBackground;
import org.xcom.mod.pojos.InstallLog;
import org.xcom.mod.pojos.ResFile;
import org.xcom.mod.pojos.XMod;
import org.xcom.mod.tools.exceptions.ExportFileAccessException;
import org.xcom.mod.tools.exceptions.UpkFileAccessException;
import org.xcom.mod.tools.exceptions.UpkFileNotDecompressedException;
import org.xcom.mod.tools.exceptions.UpkResourceNotFoundException;
import org.xcom.mod.tools.installer.exceptions.SearchInterruptedException;
import org.xcom.mod.tools.installer.exceptions.UpkFileNotFoundException;

/**
 * @author Anthony Surma
 * 
 */
final public class Installer extends Main {
	
	final static String COOKED = "\\XComGame\\CookedPCConsole\\";
	final static String DIFF_DATA_TAG = "DiffData";
	final static String SEARCH_HASH_TAG = "SearchHash";
	final static String DATA_SUM_TAG = "DataSum";
	
	private XMod installPackage;
	private List<Path> upkFiles;
	private File modFile;
	private JComponent src = null;
	
	public Installer(File modFile) {
		super();
		this.modFile = modFile;
	}
	
	public Installer(File modFile, JComponent src) {
		this(modFile);
		this.src = src;
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
			} catch (ExportFileAccessException e1) {
				ERROR = Error.INS_EXPORT_EXTRACTION;
				e1.printStackTrace(System.err);
				setDone(true);
				return;
			}
		}
		
		// Get upk files for each resource
		List<ResFile> files = installPackage.getResFiles();
		try {
			upkFiles = getResUpkOwners(files);
			
			List<ResFile> out = null;
			
			out = makeUPKChanges(files, new Vector<Path>(upkFiles), sync);
			
			installPackage.setResFiles(out);
			// TODO MAY HAVE TO INCLUDE SOME OF FULL INSTALL CHECK SO CAN
			// ROLLBACK
			installPackage.setIsInstalled(true);
			InstallLog log = new InstallLog(installPackage.getName(),
					installPackage.getAuthor(), installPackage.getDescription(), null,
					null);
			saveXml(log);
			printXml(log);
			
		} catch (UpkFileNotDecompressedException e) {
			List<Path> uncFiles = e.getFiles();
			
			String fileNames = "";
			
			for (Path p : uncFiles) {
				fileNames += (p.getFileName() + " ");
			}
			
			int n = JOptionPane.showConfirmDialog(XCMGUI.getFrame(),
					"Cannot continue. [" + fileNames + "] "
							+ (uncFiles.size() > 1 ? "are" : "is")
							+ " not decompressed do you want to decompress now?",
					"Upk file not decompressed.", JOptionPane.YES_NO_OPTION);
			
			switch (n) {
				case JOptionPane.YES_OPTION:
						new DecompressInBackGround(uncFiles, src).execute();
						break;					
				default:
			}
			ERROR = Error.INS_UPK_FILE_COMPRESSED;
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
			SearchInterruptedException, UpkResourceNotFoundException,
			XmlSaveException {
		printActionMessage("INSTALL");
		
		try {
			installPackage = (XMod) u.unmarshal(modFile);
			printXml(installPackage, "MOD FILE FOR INSTALLATION");
		} catch (JAXBException e) {
			throw new ExportFileAccessException("JAXBException");
		}
		
		// Get upk files for each resource
		List<ResFile> files = installPackage.getResFiles();
		
		upkFiles = getResUpkOwners(files);
		
		List<ResFile> out = makeUPKChanges(files, new Vector<Path>(upkFiles), sync);
		
		// TODO MAY HAVE TO INCLUDE SOME OF FULL INSTALL CHECK SO CAN
		// ROLLBACK
		
		installPackage.setResFiles(out);
		installPackage.setIsInstalled(true);
		InstallLog log = new InstallLog(installPackage.getName(),
				installPackage.getAuthor(), installPackage.getDescription(), null, null);
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
	private static List<Path> getResUpkOwners(List<ResFile> files)
			throws UpkFileNotDecompressedException, UpkFileNotFoundException,
			UpkFileAccessException {
		
		List<Path> upkFiles = new Vector<Path>(files.size());
		List<Path> uncFiles = new Vector<Path>(files.size());
		
		// Get all changed upk files from the resources
		for (ResFile f : files) {
			
			Path path = Paths.get(config.getXcomPath(), COOKED, f.getUpkFilename());
			try {
				if (Files.notExists(path)) {
					print("CANNOT FIND UPK FILE [" + path.getFileName(), "]");
					throw new UpkFileNotFoundException();
				} else if (!Files.isWritable(path)) {
					throw new IOException();
				} else if (!isDecompressed(path)) {
					if (!uncFiles.contains(path)) {
						uncFiles.add(path);
						print("UPK FILE FOUND AND IS  NOT DECOMPRESSED!!! [" + path, "]");
						continue;
					}
				}
			} catch (IOException e) {
				String msg = "CANNOT ACCESS UPK FILE [" + path.getFileName() + "]";
				print(msg, "");
				throw new UpkFileAccessException("IOException: " + msg);
				// ALL ERRORS OK
			}
			print("UPK FILE FOUND AND IS DECOMPRESSED [" + path, "]");
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
	private static List<ResFile> makeUPKChanges(List<ResFile> files,
			Vector<Path> upkFiles, RunInBackground sync)
			throws UpkResourceNotFoundException, UpkFileAccessException,
			SearchInterruptedException {
		
		print("INSTALLING FILES", "");
		int threadsNum = (NUM_THREADS > 1 ? NUM_THREADS - 1 : NUM_THREADS);
		Worker[] workers = new Worker[threadsNum];
		Thread[] threads = new Thread[threadsNum];
		
		float[] progress = calculateWorkProgress(files, upkFiles, threadsNum, sync);
		
		int i = 0;
		for (ResFile f : files) {
			Path upkFile = upkFiles.get(i);
			Boolean found = false;
			Boolean started = false;
			
			try (FileChannel fc = FileChannel.open(upkFile, StandardOpenOption.READ,
					StandardOpenOption.WRITE); FileLock lock = fc.tryLock()) {
				started = true;
				ByteBuffer buffer = ByteBuffer.allocate((int) fc.size()
						+ f.getSearchHashLength());
				
				print(INSTALL, "SEARCHING [" + upkFile.getFileName(),
						"] FOR RESOURCE [", f.getResName(), "]");
				print("BYTE SUM TO FIND [" + f.getByteSum(), "]");
				print("HASH TO FIND [", f.getSearchHash(), "]");
				print("BUFFER SIZE [" + (buffer.capacity() / 1000000.0), " MB]");
				
				fc.read(buffer);
				
				try {
					workers = doSearch(threadsNum, workers, threads, i, buffer, sync,
							progress, f);
				} catch (InterruptedException e) {
					throw new SearchInterruptedException("InterruptedException");
				}
				
				// GET WROKER RESULTS
				for (Worker w : workers) {
					
					if (w.isInstalled) {
						files.get(i).setIsInstalled(true);
					}
					if (w.resultBytes != null) {
						found = true;
						
						print("WRITING BUFFER TO RESOURCE [" + f.getResName(), "]");
						fc.position(w.resultInt);
						fc.write(ByteBuffer.wrap(w.resultBytes));
						
						print("RESOURCE SAVED", "");
						editedUpks.add(upkFile);
						if (sync != null) {
							sync.getSync().plusProgress(1);
						}
						break;
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
					
					print(
							"\n",
							"THE SYSTEM CURRENTLY DOES NOT UNDO CHANGES PLEASE DO SO MANUALLY AND REPORT ERROR.\n",
							CONSOLE_SEPARATOR);
					throw new UpkResourceNotFoundException();
				}
			}
			if (sync != null) {
				sync.getSync().plusProgress(1);
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
	private static Worker[] doSearch(int threadsNum, Worker[] workers,
			Thread[] threads, int i, ByteBuffer buffer, RunInBackground sync,
			float[] progress, ResFile f) throws InterruptedException {
		
		final CountDownLatch mainCDLatch = new CountDownLatch(1);
		final CountDownLatch workerCDLatch = new CountDownLatch(threadsNum);
		final int length = buffer.capacity() / threadsNum;
		
		print("CREATING SEARCHERS", "");
		
		for (int j = 0; j < threadsNum; ++j) {
			
			int start = j * length;
			int end = start + (length - 1);
			
			workers[j] = new Worker(f, buffer.array(), start, end, md, mainCDLatch,
					workerCDLatch, threads, j + 1, sync, progress[i] / threadsNum);
			
			threads[j] = new Thread(workers[j]);
		}
		for (Thread t : threads) {
			t.start();
		}
		mainCDLatch.await();
		print("SEARCHERS FINISHED", "");
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
	
	private static float[] calculateWorkProgress(List<ResFile> files,
			Vector<Path> upkFiles, int threadsNum, RunInBackground sync) {
		
		if (sync != null) {
			sync.getSync().plusProgress(1);
		}
		
		int i = 0;
		int size = upkFiles.size();
		float[] passesForFile = new float[size];
		
		for (ResFile f : files) {
			passesForFile[i++] = f.getChanges().size();
		}
		
		// size twice as plusProgress called twice in file loops
		float progressStart = (99 - size - size);
		float[] progress = new float[size];
		
		float sum = 0;
		i = 0;
		// Group percentage
		for (Path p : upkFiles) {
			int length = (int) p.toFile().length();
			sum += (length * passesForFile[i]);
			progress[i] = (length * passesForFile[i]);
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
