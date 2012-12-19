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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.xml.bind.JAXBException;

import org.xcom.main.shared.Main;
import org.xcom.main.shared.XmlSaveException;
import org.xcom.main.shared.entities.InstallLog;
import org.xcom.main.shared.entities.ModInstall;
import org.xcom.main.shared.entities.ResFile;
import org.xcom.main.shared.entities.XMod;
import org.xcom.mod.gui.streams.Stream;
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
	
	private final static int numWorkers = (NUM_CPU > 1 ? NUM_CPU - 1 : NUM_CPU);
	
	private XMod installPackage;
	private List<Path> upkFiles;
	private File modFile;
	private Boolean upateUpkPositionOnly = false;
	private Stream stream;
	
	private Installer(File modFile) {
		super();
		this.modFile = modFile;
	}
	
	public Installer(File modFile, Boolean upateUpkPositionOnly) {
		this(modFile);
		this.upateUpkPositionOnly = upateUpkPositionOnly;
		if (!upateUpkPositionOnly) {
			stream = INSTALL;
		} else {
			stream = MAKE;
		}
	}
	
	@Override
	public void run() {
		XMod copy = null;
		
		try {
			installPackage = (XMod) getUnMarshaller().unmarshal(modFile);
			copy = (XMod) getUnMarshaller().unmarshal(modFile);
			if (!upateUpkPositionOnly) {
				printXml(installPackage, "INSTALL XMOD");
			}  else {
				printActionMessage("GET RESOURCE UPK POSITIONS");
			}
		} catch (Exception e) {
			try {
				throw new ExportFileAccessException();
			} catch (ExportFileAccessException ex) {
				ERROR = Error.INS_EXPORT_EXTRACTION;
				setDone(true);
				return;
			}
		}

		try {
			upkFiles = findResourceUpkFile(copy.getResFiles(), stream);
			
			List<ResFile> out = makeUPKChangesAndSearch(copy.getResFiles(), upkFiles,
						upateUpkPositionOnly, stream);
			
			if (!upateUpkPositionOnly) {
				installPackage.setIsInstalled(true);
				int i = 0;
				for (ResFile f : out) {
					copy.getResFiles().get(i++).setUpkOffset(f.getUpkOffset());
				}
				
				ModInstall log = new ModInstall(installPackage, out);
				getGameState().getMods().add(installPackage);
				getGameState().getInstallData().add(log);
				saveXml(log);
				printXml(log);
				saveXml(getGameState());
			} else {
				installPackage.setResFiles(out);
			}
			saveXml(installPackage);
			
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
	 */
	public void runc() throws UpkFileNotFoundException, UpkFileAccessException,
				UpkFileNotDecompressedException, ExportFileAccessException,
				SearchInterruptedException, UpkResourceNotFoundException, XmlSaveException {
		printActionMessage("INSTALL");
		
		try {
			installPackage = (XMod) getUnMarshaller().unmarshal(modFile);
			printXml(installPackage, "MOD EXPORT FILE FOR INSTALLATION");
		} catch (JAXBException e) {
			throw new ExportFileAccessException("JAXBException");
		}
		
		// Get upk files for each resource
		List<ResFile> files = installPackage.getResFiles();
		
		upkFiles = findResourceUpkFile(files, stream);
		
		List<ResFile> out = makeUPKChangesAndSearch(files, upkFiles, upateUpkPositionOnly,
					stream);
		
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
	 */
	static List<Path> findResourceUpkFile(List<ResFile> files, Stream stream)
				throws UpkFileNotDecompressedException, UpkFileNotFoundException,
				UpkFileAccessException {
		
		List<Path> upkFiles = new ArrayList<Path>(files.size());
		List<Path> uncFiles = new ArrayList<Path>(files.size());
		
		// Get all changed upk files from the resources
		for (ResFile f : files) {
			
			Path path = Paths.get(getConfig().getCookedPath().toString(), f.getUpkFilename());
			
			if (Files.notExists(path)) {
				print(stream, "CANNOT FIND UPK [" + path.getFileName(), "]");
				throw new UpkFileNotFoundException();
			}
			try {
				if (!Files.isWritable(path)) {
					throw new IOException();
				} else if (!isDecompressed(path)) {
					if (!uncFiles.contains(path)) {
						uncFiles.add(path);
						print(stream, "UPK FOUND - NOT DECOMPRESSED [" + path.getFileName(), "]");
					}
					continue;
				}
			} catch (IOException e) {
				String msg = "CANNOT ACCESS UPK [" + path.getFileName() + "]";
				print(stream, msg, "");
				throw new UpkFileAccessException("IOException: " + msg);
			}
			print(stream, "UPK FOUND - IS DECOMPRESSED [" + path.getFileName(), "]");
			upkFiles.add(path);
		}
		if (!uncFiles.isEmpty()) throw new UpkFileNotDecompressedException(uncFiles);
		else return upkFiles;
	}
	
	/**
	 * Initiates worker threads to make Upk file changes.
	 * 
	 */
	static List<ResFile> makeUPKChangesAndSearch(List<ResFile> files, List<Path> upkFiles,
				Boolean upateUpkPositionOnly, Stream stream) throws UpkResourceNotFoundException,
				UpkFileAccessException, SearchInterruptedException {
		
		if (!upateUpkPositionOnly) {
			print(stream, "INSTALLING RESOURCE CHANGES", "");
		} else {
			print(stream, "FINDING RESOURCE POSITION IN UPK", "");
		}
		Worker[] workers = new Worker[numWorkers];
		Thread[] threads = new Thread[numWorkers];
		
		int i = 0;
		for (ResFile f : files) {
			Path upkFile = upkFiles.get(i);
			Boolean found = false;
			Boolean started = false;
			
			try (ByteScannerChannel sc = new ByteScannerChannel(upkFile);
						FileChannel ch = sc.getChannel()) {
				started = true;
				ByteBuffer buffer = ByteBuffer.wrap(new byte[(int) (ch.size())]);
				
				print(stream, CONSOLE_SEPARATOR, "");
				ch.read(buffer);
				
				if (f.getUpkOffset() == -1) {
					
					print(stream, "SEARCHING [" + upkFile.getFileName(), "] FOR RESOURCE [", f
								.getResName(), "]");
					print(stream, "CHECKSUM [" + f.getCheckSum(), "]");
					print(stream, "HASH [", MHash.toPrintString(f.getSearchHash()), "]");
					
					try {
						workers = doSearch(numWorkers, workers, threads, i, buffer, f,
									upateUpkPositionOnly);
					} catch (InterruptedException e) {
						throw new SearchInterruptedException("InterruptedException");
					}
					
				} else {
					print(stream, "VERIFYING [" + upkFile.getFileName(), "] FOR RESOURCE [", f
								.getResName(), "]");
					print(stream, "CHECKSUM [" + f.getCheckSum(), "]");
					
					print(stream, "HASH [", MHash.toPrintString(f.getSearchHash()), "]");
					try {
						workers = doSearch(workers, threads, i, buffer, f);
					} catch (InterruptedException e) {
						throw new SearchInterruptedException("InterruptedException");
					}
				}
				
				// GET WORKER RESULTS
				for (Worker w : workers) {
					
					if (w != null && w.isFound) {
						found = true;
						
						if (!upateUpkPositionOnly) {
							if (w.resultBytes != null) {
								
								if (w.endOffset == -1) {
									print(stream, "WRITING HEX EDITS [" + f.getResName(), "]");
									ch.position(w.startOffset);
									ch.write(ByteBuffer.wrap(w.resultBytes));
									
								} else if (w.endOffset != -1) {
									print(stream, "REPLACING RESOURCE [" + f.getResName(), "]");
									byte[] eof = new byte[(int) (ch.size() - w.endOffset)];
									sc.seek(w.endOffset);
									sc.readFully(eof);
									sc.seek(w.startOffset);
									sc.write(w.resultBytes);
									sc.write(eof);
								}
								
								editedUpks.add(upkFile);
								files.set(i, w.mod);
								// files.get(i).setUpkOffset(w.startOffset);
								// files.get(i).setIsInstalled(true);
								// files.get(i).setSearchHash(MHash.toString(w.newHash));
								// files.get(i).setCheckSum(w.newSum);
								// files.get(i).setSearchHashLength(w.newSize);
							}
						}
						print(stream, "SAVING RESOURCE LOCATION [" + w.startOffset, "]");
						files.get(i).setUpkOffset(w.startOffset);
					}
				}
				
			} catch (IOException e) {
				throw new UpkFileAccessException("IOException");
			} finally {
				if (!started) {
					throw new UpkFileAccessException("IOException,started");
				}
				if (!found) {
					
					print(stream, "RESOURCE NOT FOUND [" + f.getResName(),
								"] PLEASE CHECK YOUR CONFIG SETTINGS.", "");
					
					print(stream,
								"\n",
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
	 */
	static Worker[] doSearch(int threadsNum, Worker[] workers, Thread[] threads, int i,
				ByteBuffer buffer, ResFile f, Boolean upateUpkPositionOnly)
				throws InterruptedException {
		
		final CountDownLatch mainCDLatch = new CountDownLatch(1);
		final CountDownLatch workerCDLatch = new CountDownLatch(threadsNum);
		final int length = buffer.capacity() / threadsNum;
		
		for (int j = 0; j < threadsNum; ++j) {
			
			int start = j * length;
			int end = start + (length - 1) - f.getSearchHashLength();
			
			workers[j] = new Worker(f, buffer.array(), start, end, getDigest(), mainCDLatch,
						workerCDLatch, threads, j + 1, upateUpkPositionOnly);
			
			threads[j] = new Thread(workers[j]);
		}
		for (Thread t : threads) {
			t.start();
		}
		mainCDLatch.await();
		return workers;
	}
	
	/**
	 * Creates searchers and searcher threads. Does the search.
	 * 
	 */
	static Worker[] doSearch(Worker[] workers, Thread[] threads, int i, ByteBuffer buffer,
				ResFile f) throws InterruptedException {
		
		final CountDownLatch mainCDLatch = new CountDownLatch(1);
		final CountDownLatch workerCDLatch = new CountDownLatch(1);
		workers[0] = new Worker(f, buffer.array(), f.getUpkOffset(), f.getUpkOffset()
					+ f.getSearchHashLength() - 1, getDigest(), mainCDLatch, workerCDLatch,
					threads, 1, false);
		threads[0] = new Thread(workers[0]);
		threads[0].start();
		mainCDLatch.await();
		return workers;
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
