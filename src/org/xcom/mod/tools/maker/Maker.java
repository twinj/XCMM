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

package org.xcom.mod.tools.maker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBException;

import org.xcom.main.shared.CopyFileException;
import org.xcom.main.shared.Main;
import org.xcom.main.shared.XmlSaveException;
import org.xcom.main.shared.entities.HexEdit;
import org.xcom.main.shared.entities.ModConfig;
import org.xcom.main.shared.entities.ResFile;
import org.xcom.main.shared.entities.XMod;
import org.xcom.mod.tools.shared.UpkFileNotExtractedException;
import org.xcom.mod.tools.xshape.CalculateHashException;
import org.xcom.mod.tools.xshape.MHash;
import org.xcom.mod.tools.xshape.XModXmlAccessException;

/**
 * @author Anthony Surma
 * @author Daniel Kemp
 * 
 */
final public class Maker extends Main {
	
	private ModConfig modConfig;
	private volatile XMod xMod;
	
	
	public XMod getXMod() {
		return this.xMod;
	}

	public Maker() {
		super();
	}
	
	public Maker(ModConfig modConfig) {
		super();
		this.modConfig = modConfig;
	}
	
	@Override
	public void run() {
		
		try {
			saveXml(modConfig);
			copyFile(modConfig.getXmlSavePath(), Paths.get("config", "history.xml"), true);
			printXml(modConfig);
			xMod = generateXMod(modConfig);
			saveXModFiles(modConfig, xMod);
			
	
		} catch (XmlSaveException ex) {
			ERROR = Error.XML_SAVE_ERROR;
			ex.printStackTrace(System.err);
		} catch (XModXmlAccessException ex) {
			ERROR = Error.MAK_MOD_ACCESS_ERROR;
			ex.printStackTrace(System.err);
		} catch (ProcessFileChangesException | DetectUpkChangesException ex) {
			ERROR = Error.MAK_MOD_IO_ERROR;
			ex.printStackTrace(System.err);
		} catch (CalculateHashException ex) {
			ERROR = Error.MAK_HASH_GET_ERROR;
			ex.printStackTrace(System.err);
		} catch (CopyFileException ex) {
			ERROR = Error.MAK_SAVE_MOD_FILES;
			ex.printStackTrace(System.err);
		} catch (UpkFileNotExtractedException ex) {
			ERROR = Error.MAK_UPK_FILE_NOTEXTRACTED;
			ret = ex.getFiles();
			ex.printStackTrace(System.err);
		}
		setDone(true);
	}
	
	/**
	 * Run on console.
	 * 
	 */
	public void runc() throws XmlSaveException, XModXmlAccessException,
				ProcessFileChangesException, DetectUpkChangesException, CopyFileException,
				CalculateHashException, UpkFileNotExtractedException {
		
		printXml(modConfig);
		saveXml(modConfig);
		XMod xMod = generateXMod(modConfig);
		saveXModFiles(modConfig, xMod);
	}
	
	/**
	 * Saves all of the XMod files to their set out locations.
	 * 
	 * @param modConfig
	 * @param xMod
	 * @throws XmlSaveException
	 * @throws CopyFileException
	 */
	static void saveXModFiles(ModConfig modConfig, XMod xMod) throws XmlSaveException,
				CopyFileException {
		// Save Exportable mod
		print("SAVING XMOD FILES", "");
		saveXml(xMod);
		copyFiles(modConfig.getEditedFiles(), xMod.getEditedFilesSavePath(), true, false);
		copyFiles(modConfig.getOriginalFiles(), xMod.getOriginalFilesSavePath(), true, true);
		
		Path ini = null;
		
		if (modConfig.getIni() != null) {
			ini = Paths.get(modConfig.getIni());
			if (Files.exists(ini)) {
				copyFile(ini, Paths.get("mods", xMod.getName(), ini.getFileName().toString()),
							true);
			}
		}
	}
	
	/**
	 * Save either modified or original mod resources to the mod directory.
	 */
	static void copyFiles(List<Path> files, Path path, Boolean replaceExisting,
				Boolean alternatePaths) throws CopyFileException {
		
		for (Path f : files) {
			
			Path p = null;
			
			if (alternatePaths) {
				int unpackedECount = Paths.get(getConfig().getUnpackedPath()).toAbsolutePath()
							.getNameCount();
				p = f.toAbsolutePath().getParent();
				p = Paths.get(path.toString(), p.subpath(unpackedECount, p.getNameCount())
							.toString());
			}
			
			if (p != null && Files.notExists(p)) {
				try {
					Files.createDirectories(p);
				} catch (IOException ex) {
					throw new CopyFileException();
				}
			}
			copyFile(f, Paths.get((p == null ? path.toString() : p.toString()), f.getFileName()
						.toString()), replaceExisting);
		}
	}
	
	/**
	 * Gets Upk filename
	 */
	static String getUpkFilename(Path resource) {
		
		int unpackedECount = Paths.get(getConfig().getUnpackedPath()).toAbsolutePath()
					.getNameCount();
		String ret = resource.toAbsolutePath().subpath(unpackedECount, unpackedECount + 1)
					.toString()
					+ ".upk";
		print("UPK FILENAME [", ret, "]");
		return ret;
	}
	
	/**
	 * Process the file changes and creates the actual module.
	 */
	static XMod generateXMod(ModConfig monfig) throws UpkFileNotExtractedException,
				XModXmlAccessException, ProcessFileChangesException, DetectUpkChangesException,
				CalculateHashException {
		
		print("MOD GENERATE ACTION", "");
		
		XMod xMod = new XMod();
		Vector<ResFile> changes;
		
		changes = processResourceChanges(monfig);
		
		xMod.setResFiles(changes);
		xMod.setName(monfig.getName());
		xMod.setAuthor(monfig.getAuthor());
		xMod.setDescription(monfig.getDescription());
		xMod.setModVersion(monfig.getVersion());
		xMod.setGameVersion(getGameVersion());
		
		if (monfig.getIni() != null) {
			xMod.setIni(Paths.get(monfig.getIni()).getFileName().toString());
		}
		
		// Write the xml unmarshalled object
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			
			getMarshaller().marshal(xMod, baos);
			byte[] b = baos.toByteArray();
			
			// Calculate hash
			getDigest().update(b, 0, b.length);
			b = getDigest().digest();
			xMod.setHash(MHash.toString(b));
			getDigest().reset();
			
			print("MOD HASH [", MHash.toPrintString(b), "]");
			printXml(xMod);
		} catch (JAXBException e) {
			throw new XModXmlAccessException();
		} catch (IOException ignore) {
			ignore.printStackTrace(System.err);
		}
		return xMod;
	}
	
	/**
	 * Processes the file changes returning a list of the files changed and the
	 * data changed.
	 * 
	 * @param modConfig
	 * @return
	 * @throws ProcessFileChangesException
	 * @throws DetectUpkChangesException
	 * @throws CalculateHashException
	 * @throws UpkFileNotExtractedException
	 */
	static Vector<ResFile> processResourceChanges(ModConfig mConfig)
				throws UpkFileNotExtractedException, CalculateHashException,
				ProcessFileChangesException, DetectUpkChangesException {
		
		Vector<ResFile> changes = new Vector<ResFile>();
		Vector<Path> uncFiles = new Vector<Path>();
		
		int i = 0;
		
		// For each modded file get changes
		for (String path : mConfig.getOriginalFilePaths()) {
			
			Path originalResource = Paths.get(getConfig().getUnpackedPath(), path);
			String upkFileName = getUpkFilename(originalResource);
			Path upk = Paths.get(getConfig().getCookedPath().toString(), upkFileName);
			
			// Test is upk is uncompressed and unpacked so can create mod if not do
			// work
			if (Files.notExists(originalResource)) {
				if (!uncFiles.contains(upk)) {
					uncFiles.add(upk);
					print("UPK NOT UNPACKED [" + upkFileName, "]");
				}
				continue;
			}
			
			if (uncFiles.isEmpty()) {
				
				MHash hash = new MHash(originalResource);
				
				print("PROCESSING ORIGINAL [" + originalResource.getFileName(), "] FROM ["
							+ upkFileName + "]");
				print("SEARCH HASH [", hash.toPrintString(), "]");
				
				final long sum = getResourceCheckSum(originalResource);
				
				ResFile f = null;
				try {
					f = new ResFile(null, originalResource.getFileName().toString(), upkFileName,
								hash.toString(), (int) Files.size(originalResource), (int) sum, -1);
				} catch (IOException e) {
					throw new ProcessFileChangesException("IOException,size");
				}
				
				f.setChanges(getUPKChanges(originalResource, mConfig.getEditedFiles().get(i)));
				
				if (f.getChanges().get(0).getOffset() == -1) {
					f.setIsSameSize(false); // will replace whole resource
				}
				changes.add(f);
				printXml(f);
				++i;
			}
		}
		if (!uncFiles.isEmpty()) throw new UpkFileNotExtractedException(uncFiles);
		else return changes;
	}
	
	/**
	 * Sum the byte contents of the file to help with confirming hash
	 */
	static int getResourceCheckSum(Path path) throws ProcessFileChangesException {
		int sum = 0;
		byte[] bytes = null;
		try {
			bytes = Files.readAllBytes(path);
		} catch (IOException e) {
			throw new ProcessFileChangesException("IOException,readAllBytes");
		}
		
		for (int i = 0; i < bytes.length; i++) {
			sum += bytes[i] & 0xFF;
		}
		print("RESOURCE CHECKSUM [" + sum, "]");
		return sum;
	}
	
	/**
	 * Detect the changes made to the UPK file and return a List containing the
	 * offset position and change data.
	 */
	static Vector<HexEdit> getUPKChanges(Path originalPath, Path modifedPath)
				throws DetectUpkChangesException {
		
		Vector<HexEdit> list = new Vector<HexEdit>();
		
		try (InputStream modified = Files.newInputStream(modifedPath);
					InputStream original = Files.newInputStream(originalPath)) {
			
			print("COMPARING ORIGINAL [" + originalPath.getFileName() + "] WITH EDITED ["
						+ modifedPath.getFileName(), "]");
			
			if (Files.size(modifedPath) != Files.size(originalPath)) {
				print("RESOURCE FILE SIZES ARE DIFFERENT ASSUMING YOU WANT TO OVERWITE RESOURCE");
				String hexStringDataFull = null;
				String hexStringBackupFull = null;
				
				byte[] data = new byte[(int) Files.size(modifedPath)];
				modified.read(data);
				hexStringDataFull = MHash.toString(data);
				
				data = new byte[(int) Files.size(originalPath)];
				original.read(data);
				hexStringBackupFull = MHash.toString(data);
				
				list.add(new HexEdit(null, -1, hexStringDataFull, hexStringBackupFull));
			} else {
				
				int offset = 0, change;
				
				while ((change = modified.read()) >= 0) {
					int base = original.read();
					if (change != base) {
						String hexStringData = Integer.toHexString(change).toUpperCase();
						String hexStringBackup = Integer.toHexString(base).toUpperCase();
						
						if (hexStringData.length() == 1) hexStringData = "0" + hexStringData;
						if (hexStringBackup.length() == 1) hexStringBackup = "0" + hexStringBackup;
						
						list.add(new HexEdit(null, offset, hexStringData, hexStringBackup));
						print("CHANGE DETECTED @ OFFEST [" + offset, "] EDIT [", hexStringData,
									"] BACKUP BASE [", hexStringBackup, "]");
					}
					++offset;
				}
			}
		} catch (IOException e) {
			throw new DetectUpkChangesException("IOException,InputStream,read");
		}
		return list;
	}
	static void print(String... strings) {
		print(MAKE, strings);
	}
	
	/**
	 * Gui only. Determines the progress to be allocated for each part of work to
	 * be done.
	 */
	static float[] calculateWorkProgress(ModConfig mConfig) {
		List<Path> editedFiles = mConfig.getOriginalFiles();
		int i = 0;
		int size = editedFiles.size();
		// fileSum - size; fileDone - size; getHaSh - size; save; copy
		float progressStart = (99 - (size - size - size - size));
		
		float[] progress = new float[size];
		
		int sum = 0;
		// Group percentage
		for (Path f : editedFiles) {
			int length = (int) f.toFile().length();
			sum += length;
			progress[i] = length;
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
