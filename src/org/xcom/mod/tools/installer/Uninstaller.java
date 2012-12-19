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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.xcom.main.shared.Main;
import org.xcom.main.shared.XmlSaveException;
import org.xcom.main.shared.entities.ModInstall;
import org.xcom.main.shared.entities.XMod;
import org.xcom.mod.gui.streams.Stream;
import org.xcom.mod.tools.shared.ExportFileAccessException;
import org.xcom.mod.tools.shared.UpkFileAccessException;
import org.xcom.mod.tools.shared.UpkFileNotDecompressedException;
import org.xcom.mod.tools.shared.UpkResourceNotFoundException;

/**
 * @author Anthony Surma
 * @author Daniel Kemp
 * 
 */
final public class Uninstaller extends Main {
		
	private XMod installPackage;
	private ModInstall uninstallPackage;
	private List<Path> upkFiles;
	private File uninstallFile;
	private Stream stream;
	
	public Uninstaller(File uninstallFile) {
		super();
		this.uninstallFile = uninstallFile;
		stream = UNINSTALL;
	}
	
	@Override
	public void run() {
		ModInstall copy = null;
		
		try {
			uninstallPackage = (ModInstall) getUnMarshaller().unmarshal(uninstallFile);
			Path path = uninstallPackage.getXModPath();
			if (Files.exists(path)) {
				installPackage = (XMod) getUnMarshaller().unmarshal(path.toFile());
			}
			
			copy = (ModInstall) getUnMarshaller().unmarshal(uninstallFile);
			printXml(uninstallPackage, "UNINSTALL XMOD");
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
			upkFiles = Installer.findResourceUpkFile(copy.getResFiles(), stream);
			
			Installer.makeUPKChangesAndSearch(copy.getResFiles(), upkFiles, false, stream);
			
			if (installPackage != null) {
				installPackage.setIsInstalled(false);
				saveXml(installPackage);
			}	
			
			uninstallPackage.setIsInstalled(false);
			Files.delete(uninstallFile.toPath());
			
			// getGameState().getMods().add(installPackage);
			// getGameState().getInstallData().remove(index)(log);
			// saveXml(getGameState());
			
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
		} catch (IOException ex) {
			// TODO Delete error should not occur
			ex.printStackTrace();
		}
		setDone(true);
	}
	
	/**
	 * Run on console.
	 */
	public void runc() throws UpkFileNotFoundException, UpkFileAccessException,
				UpkFileNotDecompressedException, ExportFileAccessException,
				SearchInterruptedException, UpkResourceNotFoundException, XmlSaveException {
	
	}

	public XMod getInstallPackage() {
		return installPackage;
	}
	
	public ModInstall getUninstallPackage() {
		return uninstallPackage;
	}
	
	public List<Path> getUpkFiles() {
		return upkFiles;
	}
	
}
