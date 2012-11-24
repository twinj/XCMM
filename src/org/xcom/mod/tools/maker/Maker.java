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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.xcom.mod.Main;
import org.xcom.mod.exceptions.XmlSaveException;
import org.xcom.mod.gui.CopyFileException;
import org.xcom.mod.gui.workers.RunInBackground;
import org.xcom.mod.pojos.HexEdit;
import org.xcom.mod.pojos.ModConfig;
import org.xcom.mod.pojos.ResFile;
import org.xcom.mod.pojos.XMod;
import org.xcom.mod.tools.maker.exceptions.DetectUpkChangesException;
import org.xcom.mod.tools.maker.exceptions.ProcessFileChangesException;
import org.xcom.mod.tools.xshape.MHash;
import org.xcom.mod.tools.xshape.exceptions.CalculateHashException;
import org.xcom.mod.tools.xshape.exceptions.XModXmlAccessException;

/**
 * @author Anthony Surma
 * 
 */
final public class Maker extends Main {
    
    private ModConfig modConfig;
    
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
            printXml(modConfig);
            XMod xMod = generateXMod(modConfig, sync);
            saveXModFiles(modConfig, xMod);
            sync.getSync().plusProgress(1 * modConfig.getEditedFiles().size());
            
        } catch (XmlSaveException e) {
            ERROR = Error.XML_SAVE_ERROR;
            e.printStackTrace(System.err);
        } catch (XModXmlAccessException e) {
            ERROR = Error.MAK_MOD_ACCESS_ERROR;
            e.printStackTrace(System.err);
        } catch (ProcessFileChangesException | DetectUpkChangesException e) {
            ERROR = Error.MAK_MOD_IO_ERROR;
            e.printStackTrace(System.err);
        } catch (CalculateHashException e) {
            ERROR = Error.MAK_HASH_GET_ERROR;
            e.printStackTrace(System.err);
        } catch (CopyFileException e) {
            ERROR = Error.MAK_SAVE_MOD_FILES;
            e.printStackTrace(System.err);
        }
        setDone(true);
    }
    
    /**
     * Run on console.
     * 
     * @throws XmlSaveException
     * @throws DetectUpkChangesException
     * @throws ProcessFileChangesException
     * @throws XModXmlAccessException
     * @throws CopyFileException
     * @throws CalculateHashException
     */
    public void runc() throws XmlSaveException, XModXmlAccessException,
                ProcessFileChangesException, DetectUpkChangesException,
                CopyFileException, CalculateHashException {
        
        printXml(modConfig);
        saveXml(modConfig);
        XMod xMod = generateXMod(modConfig, null);
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
    private static void saveXModFiles(ModConfig modConfig, XMod xMod)
                throws XmlSaveException, CopyFileException {
        // Save Exportable mod
        print(CONSOLE_SEPARATOR);
        print("SAVING XMOD FILES", "");
        saveXml(xMod);
        copyFiles(modConfig.getEditedFiles(), xMod.getEditedFilesSavePath(), false);
        copyFiles(modConfig.getOriginalFiles(), xMod.getOriginalFilesSavePath(), true);
    }
    
    /**
     * Process the file changes and creates the actual module.
     * 
     * @param monfig
     * @param sync
     * @return
     * @throws XModXmlAccessException
     * @throws ProcessFileChangesException
     * @throws DetectUpkChangesException
     * @throws CalculateHashException
     */
    final public static XMod generateXMod(ModConfig monfig, RunInBackground sync)
                throws XModXmlAccessException, ProcessFileChangesException,
                DetectUpkChangesException, CalculateHashException {
        
        print(CONSOLE_SEPARATOR);
        print("MOD GENERATE ACTION", "");
        
        XMod xMod = new XMod();
        List<ResFile> changes = processFileChanges(monfig, sync);
        
        xMod.setResFiles(changes);
        xMod.setName(monfig.getName());
        xMod.setAuthor(monfig.getAuthor());
        xMod.setDescription(monfig.getDescription());
        
        print(CONSOLE_SEPARATOR);
        
        // Write the xml unmarshalled object
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            m.marshal(xMod, baos);
            byte[] b = baos.toByteArray();
            
            // Calculate hash
            md.update(b, 0, b.length);
            xMod.setHash(MHash.toString(md.digest()));
            md.reset();
            
            print("MOD HASH [", xMod.getHash(), "]");
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
     * @param sync
     * @param modConfig
     * @return
     * @throws ProcessFileChangesException
     * @throws DetectUpkChangesException
     * @throws CalculateHashException
     */
    final public static List<ResFile> processFileChanges(ModConfig mConfig,
                RunInBackground sync) throws ProcessFileChangesException,
                DetectUpkChangesException, CalculateHashException {
        List<ResFile> changes = new ArrayList<ResFile>();
        
        int[] progress = calculateWorkProgress(mConfig);
        int i = 0;
        
        // For each modded file get changes
        for (String path : mConfig.getOriginalFilePaths()) {
            
            Path originalResource = Paths.get(config.getUnpackedPath() + path);
            
            String hash = MHash.getHashString(originalResource);
            
            if (sync != null) {
                sync.getSync().plusProgress(1);
            }
            
            print("ORIGINAL FILE SEARCH HASH [", hash, "]");
            
            ResFile f = null;
            
            try {
                f = new ResFile(null, originalResource.getFileName().toString(),
                            getUPKFilename(originalResource), hash, (int) Files
                                        .size(originalResource),
                            getDataSum(originalResource));
                if (sync != null) {
                    sync.getSync().plusProgress(1);
                }
            } catch (IOException e) {
                throw new ProcessFileChangesException("IOException,size");
            }
            
            f.setChanges(getUPKChanges(originalResource, mConfig.getEditedFiles().get(i)
                        .toPath(), progress[i], sync));
            
            changes.add(f);
            printXml(f);
            ++i;
            if (sync != null) {
                sync.getSync().plusProgress(1);
            }
        }
        return changes;
    }
    
    /**
     * Gui only. Determines the progress to be allocated for each part of work
     * to be done.
     * 
     * @param mConfig
     * @return
     */
    private static int[] calculateWorkProgress(ModConfig mConfig) {
        List<File> editedFiles = mConfig.getEditedFiles();
        int i = 0;
        int size = editedFiles.size();
        // fileSum - size; fileDone - size; getHaSh - size; save; copy
        int progressStart = (99 - (size - size - size - size));
        
        int[] progress = new int[size];
        
        int sum = 0;
        // Group percentage
        for (File f : editedFiles) {
            int length = (int) f.length();
            sum += length;
            progress[i] = length;
            i++;
        }
        i = 0;
        for (int p : progress) {
            int n = (p / sum) * progressStart;
            progress[i] = n;
        }
        return progress;
    }
    
    /**
     * Sum the byte contents of the file to help with confirming hash
     * 
     * @param path
     * 
     * @return sum
     * @throws ProcessFileChangesException
     * 
     */
    static int getDataSum(Path path) throws ProcessFileChangesException {
        int sum = 0;
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new ProcessFileChangesException("IOException,readAllBytes");
        }
        for (int i = bytes.length - 1; i >= 0; --i) {
            sum += bytes[i];
        }
        print("FILE BYTE SUM [" + sum, "]");
        return sum;
    }
    
    /**
     * Detect the changes made to the UPK file and return a List containing the
     * offset position and change data.
     * 
     * @param originalPath
     *            path to original file
     * @param progress
     * @param sync
     * @param modifiedPath
     *            path to modified file
     * 
     * @return a List of changes
     * @throws DetectUpkChangesException
     */
    static List<HexEdit> getUPKChanges(Path originalPath, Path modifedPath, int progress,
                RunInBackground sync) throws DetectUpkChangesException {
        
        List<HexEdit> list = new ArrayList<HexEdit>();
        
        try (InputStream modified = Files.newInputStream(modifedPath);
             InputStream original = Files.newInputStream(originalPath)) {
            
            // Compare files save offset position and data change
            int offset = 0, change;
            
            int progressPlus = (int) (modifedPath.toFile().length() / progress);
            
            while ((change = modified.read()) >= 0) {
                
                // If modified not equal to original
                if (change != original.read()) {
                    String hexString = Integer.toHexString(change);
                    
                    if (hexString.length() == 1) hexString = "0" + hexString;
                    
                    list.add(new HexEdit(null, offset, hexString));
                    print("CHANGE DETECTED OFFEST [" + offset, "] EDIT: [", hexString,
                                "]");
                    
                    // Do not break comparison loop at end as there may be more
                    // changes
                }
                if (sync != null) {
                    // check progress
                    if (offset % progressPlus == progressPlus - 1) {
                        sync.getSync().plusProgress(1);
                        progress--;
                    }
                }
                ++offset; // Increment offset change position
            }
        } catch (IOException e) {
            throw new DetectUpkChangesException("IOException,InputStream,read");
        }
        return list;
    }
    
    private static void print(String... strings) {
        print(MAKE, strings);
    }
    
}
