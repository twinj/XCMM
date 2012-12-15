package org.xcom.mod.tools.xshape;

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.xcom.main.shared.Main;
import org.xcom.mod.gui.streams.Stream;
import org.xcom.mod.gui.workers.DecompressInBackGround;
import org.xcom.mod.tools.shared.ByteScannerChannel;
import org.xcom.mod.tools.shared.UpkFileNotDecompressedException;

/**
 * @author Anthony Surma
 */
public class XShape extends Main {
	
	private final static String VERSION = "2.0d";
	private final static String CONFIG = "XSHAPE.config";
	private final static int SEPARATOR = 0x00;
	private static long START_POSITION = 26420592; // location after 0x0d0a before
	// rcdata 1020 512KB buffer
	// after
	
	private static long END_POSITION = 26557951; // location after 0x0d0a before
	
	// rcdata 1020 512KB buffer
	// after
	private static Stream STREAM = MAIN;
	
	private List<Path> paths = null;
	private Path toPatch = null;
	private Path ini = null;
	
	/**
	 * 
	 * @param toPatch
	 * @param paths
	 * @param stream
	 * @throws IOException
	 */
	public XShape(Path toPatch, List<Path> paths, Stream stream) throws IOException {
		
		super();
		this.toPatch = toPatch;
		// This list requires duplicates before this however now they are
		// redundant and need to be trimmed for clean patching
		List<Path> cleanPaths = new Vector<Path>();
		
		for (Path p : paths) {
			if (!cleanPaths.contains(p)) {
				cleanPaths.add(p);
			}
		}
		STREAM = stream;
		this.paths = cleanPaths;
		
		makeSessionBackup(toPatch);
	}
	
	/**
	 * 
	 * @param toPatch
	 * @param paths
	 * @param stream
	 * @throws IOException
	 */
	public XShape(Path toPatch, List<Path> paths, Path ini, Stream stream)
				throws IOException {
		this(toPatch, paths, stream);
		this.ini = ini;
	}
	
	/**
	 * 
	 * @param toPatch
	 * @param ini
	 * @param stream
	 * @throws IOException
	 */
	public XShape(Path toPatch, Path ini, Stream stream) throws IOException {
		
		super();
		this.toPatch = toPatch;
		this.ini = ini;
		STREAM = stream;
		
		makeSessionBackup(toPatch);
	}
	
	public void makeSessionBackup(Path toPatch) throws IOException {
		print("CREATING A SESSION BACKUP", "");
		
		Files.copy(toPatch, Paths.get("temp", toPatch.getFileName().toString()),
					StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * 
	 * @param toPatch
	 * @param paths
	 * @param stream
	 * @param startByteOffset
	 * @throws IOException
	 */
	public XShape(Path toPatch, List<Path> paths, Stream stream, long startByteOffset)
				throws IOException {
		this(toPatch, paths, stream);
		START_POSITION = startByteOffset;
	}
	
	/**
	 * 
	 * @param toPatch
	 * @param ini
	 * @param stream
	 * @param startByteOffset
	 * @throws IOException
	 */
	public XShape(Path toPatch, Path ini, Stream stream, long startByteOffset)
				throws IOException {
		this(toPatch, ini, stream);
		START_POSITION = startByteOffset;
	}
	
	/**
	 * 
	 * @param toPatch
	 * @param ini
	 * @param stream
	 * @param startByteOffset
	 * @throws IOException
	 */
	public XShape(Path toPatch, List<Path> paths, Path ini, Stream stream,
				long startByteOffset) throws IOException {
		this(toPatch, paths, ini, stream);
		START_POSITION = startByteOffset;
	}
	
	@Override
	public void run() {
		
		printActionMessage("XSHAPE");
		print(CONSOLE_SEPARATOR, "");
		
		int numPatched = 0;
		
		try {
			
			if (paths != null) {
				List<Path> uncompressedFiles = new ArrayList<>();
				DecompressInBackGround work = null;
				
				for (final Path p : paths) {
					if (!isDecompressed(p)) {
						uncompressedFiles.add(p);
					}
				}
				if (!uncompressedFiles.isEmpty()) {
					work = new DecompressInBackGround(uncompressedFiles);
					work.execute();
				}
				
				if (work == null || work.isDone()) {
					numPatched = patchExeUpkHashs(toPatch, new Vector<MHash>(MHash
								.calculateHashes(paths)), START_POSITION, END_POSITION);
					
					final int numFiles = paths.size();
					
					if (numFiles != numPatched) {
						print("[" + (numFiles - numPatched), "] HASH NOT UPDATED.");
						print("[" + numPatched, "] FILE(S) HASH UPDATED.");
					} else {
						print("[" + numPatched, "] FILE(S) HASH UPDATED.");
					}
				} else {
					throw new UpkFileNotDecompressedException();
				}
			}
			if (ini != null) {
				patchExeIniFile(toPatch, ini, START_POSITION, END_POSITION);
			}
		} catch (CalculateHashException ex) {
			ERROR = Error.XSHA_HASH_GET_ERROR; //
			ex.printStackTrace(System.err);
		} catch (IOException ex) {
			ERROR = Error.XSHA_MOD_ACCESS_ERROR;
			ex.printStackTrace(System.err);
		} catch (UpkFileNotDecompressedException ex) {
			ERROR = Error.XSHA_UPK_FILE_COMPRESSED;
			ex.printStackTrace(System.err);
		} catch (IvalildUpkFileNameToPatch ex) {
			ERROR = Error.XSHA_UPK_FILENAME_ERROR;
			ex.printStackTrace(System.err);
		} catch (PatchNotRequired ex) {
			ERROR = Error.XSHA_PATCH_NOT_REQUIRED;
			ex.printStackTrace(System.err);
		} catch (IniFileException ex) {
			ERROR = Error.XSHA_INI_PATCHERROR;
			ex.printStackTrace(System.err);
		}
		
		setDone(true);
	}
	
	/**
	 * Console run.
	 * 
	 * @throws CalculateHashException
	 * @throws UpkFileNotDecompressedException
	 * @throws IOException
	 * @throws IvalildUpkFileNameToPatch
	 * @throws PatchNotRequired
	 * @throws IniFileException
	 */
	public void runc() throws CalculateHashException, UpkFileNotDecompressedException,
				IOException, IvalildUpkFileNameToPatch, PatchNotRequired, IniFileException {
		
		printActionMessage("XSHAPE");
		
		for (Path p : paths) {
			if (!isDecompressed(p)) {
				decompress(p); // WAITS
				sortGameFiles(p);
			}
		}
		if (paths != null) {
			patchExeUpkHashs(toPatch, new Vector<MHash>(MHash.calculateHashes(paths)),
						START_POSITION, END_POSITION);
		}
		
		if (ini != null) {
			patchExeIniFile(toPatch, ini, START_POSITION, END_POSITION);
		}
	}
	
	public void patchIni() {
		try {
			patchExeIniFile(toPatch, ini, START_POSITION, END_POSITION);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IniFileException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param sync
	 * @throws IOException
	 * @throws IvalildUpkFileNameToPatch
	 * @throws PatchNotRequired
	 */
	static int patchExeUpkHashs(Path toPatch, Vector<MHash> hashes, long startByteOffset,
				long endByteOffset) throws IOException, IvalildUpkFileNameToPatch,
				PatchNotRequired {
		
		int numPatched = 0;
		
		try (ByteScannerChannel sc = new ByteScannerChannel(toPatch);
					FileChannel ch = sc.getChannel()) {
			
			final int bufferSize = (int) (endByteOffset - startByteOffset);
			ByteBuffer buffer = ByteBuffer.wrap(new byte[bufferSize]);
			
			ch.read(buffer, startByteOffset);
			
			// Output to console
			print("STARTING AT OFFSET [" + startByteOffset, "]");
			print("ENDING AT OFFSET [" + endByteOffset, "]");
			print("BUFFER SIZE [" + bufferSize / 1000.0, " KB]");
			
			numPatched = patchUpk(toPatch, hashes, startByteOffset, sc, ch, buffer);
		} finally {
			
			// Check and report errors
			if (numPatched == 0) {
				print("PATCHING NOT REQUIRED.", "");
				throw new PatchNotRequired();
			}
		}
		return numPatched;
	}
	
	static int patchUpk(Path toPatch, Vector<MHash> hashes, long startByteOffset,
				ByteScannerChannel sc, FileChannel ch, ByteBuffer buffer) throws IOException,
				IvalildUpkFileNameToPatch {
		
		int numPatched = 0;
		
		// For each file and its hash try
		for (MHash hash : hashes) {
			
			String search = hash.getPathFileName().toLowerCase();
			
			buffer.rewind();
			byte[] fileName = hash.getFileNameBytes();
			
			print("SEARCHING FOR ENTRY [", search, "] AS BYTES [", MHash
						.toPrintString(fileName), "] IN [" + toPatch.getFileName(), "]");
			
			long position = sc.findBytes(buffer, fileName);
			
			if (position != -1) {
				buffer.position((int) (position + fileName.length));
				
				// find current file name in lowercase as a byte array
				if (buffer.get() == SEPARATOR) {
					byte[] foundHash = new byte[MHash.HASH_OUTPUT];
					
					print("ENTRY FOUND AT OFFSET ["
								+ (buffer.position() - fileName.length + startByteOffset - 1), "]");
					
					buffer.mark();
					buffer.get(foundHash);
					buffer.reset();
					
					print("EXE HASH [", MHash.toPrintString(foundHash), "]");
					print("UPK HASH [", hash.toPrintString(), "]");
					
					// Write new hashes
					if (!Arrays.equals(hash.getHash(), foundHash)) {
						ch.position(startByteOffset + buffer.position());
						ch.write(ByteBuffer.wrap(hash.getHash()));
						print("UPDATED [" + search, "] EXE HASH");
						++numPatched;
					}
				} else {
					throw new IvalildUpkFileNameToPatch();
				}
			} else {
				throw new IvalildUpkFileNameToPatch();
			}
		}
		return numPatched;
	}
	
	/**
	 * 
	 * @param sync
	 * @throws IOException
	 * @throws IvalildUpkFileNameToPatch
	 * @throws PatchNotRequired
	 * @throws IniFileException
	 * @throws FileNotFoundException
	 */
	static void patchExeIniFile(Path toPatch, Path ini, long startByteOffset,
				long endByteOffset) throws IniFileException, FileNotFoundException, IOException {
		
		try (ByteScannerChannel sc = new ByteScannerChannel(toPatch);
					FileChannel ch = sc.getChannel()) {
			
			final int bufferSize = (int) (endByteOffset - startByteOffset);
			ByteBuffer buffer = ByteBuffer.wrap(new byte[bufferSize]);
			ch.read(buffer, startByteOffset);
			
			// Output to console
			print("STARTING AT OFFSET [" + startByteOffset, "]");
			print("ENDING AT OFFSET [" + endByteOffset, "]");
			print("BUFFER SIZE [" + bufferSize / 1000.0, " KB]");
			
			buffer.rewind();
			
			patchIni(toPatch, ini, startByteOffset, sc, ch, buffer);
		}
	}
	
	static void patchIni(Path toPatch, Path ini, long startByteOffset,
				ByteScannerChannel sc, FileChannel ch, ByteBuffer buffer)
				throws IniFileException, IOException {
		String search = "[XComGame.";// XGTacticalGameCore]";
		byte[] header = search.getBytes(Main.DEFAULT_FILE_ENCODING);
		// byte[] END_PAD = new byte[]{
		// 0x0D, 0x0A, 0x50, 0x41, 0x44
		// };
		
		byte[] bytes = null;
		try {
			bytes = Files.readAllBytes(ini);
		} catch (IOException e) {
			throw new IniFileException("IOException,readAllBytes");
		}
		
		print("SEARCHING FOR RESOURCE HEADER [", search, "] AS BYTES [", MHash
					.toPrintString(header), "] IN [" + toPatch.getFileName(), "]");
		
		long sPos = sc.findBytes(buffer, header) - 2;
		long ePos = -1;
		
		// find current file name in lowercase as a byte array
		if (sPos != -1) {
			
			buffer.position((int) sPos + header.length + 2); // + 1 so as to step
																												// ahead
			// not to reread search
			// pattern
			
			print("ENTRY FOUND AT OFFSET [" + (sPos + startByteOffset), "]");
			
			ePos = sc.findBytes(buffer, header);
			buffer.position((int) ePos + header.length);
			ePos = sc.findBytes(buffer, header) - 2; // padded by 2 bytes
			
			print("END FOUND AT OFFSET [" + (ePos + startByteOffset), "]");
			
			byte[] iniBackup = new byte[(int) (ePos - sPos)];
			buffer.position((int) (sPos)); //
			sc.seek(startByteOffset + sPos);
			sc.readFully(iniBackup);			
			buffer.position((int) (ePos)); //			
			
			byte[] eof = new byte[(int) (ch.size() - buffer.position() - startByteOffset)];
			sc.seek(startByteOffset + ePos);
			sc.readFully(eof);
			sc.seek(startByteOffset + sPos);
			sc.write(bytes);
			sc.write(eof);
			
			Files.write(Paths.get(config.getUnpackedPath(), "install.ini"), iniBackup);
		}
	}
	
	private static void print(String... strings) {
		print(STREAM, strings);
	}
}
