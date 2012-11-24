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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.xcom.mod.Main;
import org.xcom.mod.gui.workers.DecompressInBackGround;
import org.xcom.mod.gui.workers.RunInBackground;
import org.xcom.mod.tools.exceptions.UpkFileNotDecompressedException;
import org.xcom.mod.tools.xshape.exceptions.CalculateHashException;

/**
 * @author Anthony Surma
 */
public class XShape extends Main implements Runnable {
    
    final static String VERSION = "2.0a";
    final static String CONFIG = "XSHAPE.config";
    final static String PERIOD = "\\.";
    final static int SEPARATOR = 0x00;
    final static int BUFFERPAD = 100;
    
    private List<Path> paths;
    private Path toPatch;
    
    public XShape(Path toPatch, List<Path> paths) {
        
        super();
        this.toPatch = toPatch;
        this.paths = paths;
    }
    
    @Override
    public void run() {
        
        printActionMessage("XSHAPE");
        sync.getSync().plusProgress(1);
        
        try {
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
                startPatching(toPatch, new Vector<MHash>(MHash.calculateHashes(paths)),
                            sync);
            } else {
                throw new UpkFileNotDecompressedException();
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
        }
        
        setDone(true);
    }
    
    /**
     * Console run.
     * 
     * @throws CalculateHashException
     * @throws UpkFileNotDecompressedException
     * @throws IOException
     */
    public void runc() throws CalculateHashException, UpkFileNotDecompressedException,
                IOException {
        
        printActionMessage("XSHAPE");
  
        for (Path p : paths) {
            if (!isDecompressed(p)) {
                
                decompress(p); // WAITS
                sortGameFiles(p);
                
                //throw new UpkFileNotDecompressedException();
            }             
        }
        
        
        startPatching(toPatch, new Vector<MHash>(MHash.calculateHashes(paths)), sync);
    }
    
    /**
     * 
     * @param sync
     * @throws IOException
     */
    static void startPatching(Path toPatch, Vector<MHash> hashes, RunInBackground sync)
                throws IOException {
        
        int numFiles = hashes.size();
        int numFound = 0;
        
        if (sync != null) {
            sync.getSync().plusProgress(1 * numFiles);
        }
        
        print("CREATING A SESSION BACKUP");
        
        Files.copy(toPatch, Paths.get("temp", toPatch.getFileName().toString()));
        
        try (FileChannel fc = FileChannel.open(toPatch, StandardOpenOption.READ,
                         StandardOpenOption.WRITE);
             FileLock lock = fc.tryLock()) {
            
            final long startAtByteNum = 20000000;
            final int bufferSize = (int) (fc.size() - startAtByteNum + BUFFERPAD);
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            
            // Output to console
            print("[" + fc.size(), "] BYTES IN [" + toPatch.getFileName(), "]");
            print("STARTING AT OFFSET [" + startAtByteNum, "]");
            print("BUFFER SIZE [" + bufferSize, "]");
            
            fc.position(startAtByteNum);
            fc.read(buffer);
            
            // fileSum - size; inconsequential to original algorithm
            int progressStart = (99 - (numFiles * 2));
            int progressAllocation = progressStart / numFiles;
            int progressPlusRate = bufferSize / progressAllocation;
            
            // For each file and its hash try
            for (MHash hash : hashes) {
                
                int p = progressPlusRate;
                int position = 1;
                
                final String lowerCaseFileName = hash.getPathFileName().toLowerCase();
                print("SEARCHING FOR ENTRY [", lowerCaseFileName, "] AS BYTES [", MHash
                            .toPrintString(hash.getFileNameBytes()), "]");
                
                // Loop buffer from start to end of buffer
                while (buffer.hasRemaining()) {
                    ByteBuffer fileName = ByteBuffer.wrap(hash.getFileNameBytes());
                    
                    // find current file name in lowercase as a byte array
                    while (fileName.hasRemaining()) {
                        if (buffer.get() == fileName.get()) {} else {
                            break;
                        }
                    }
                    if (!fileName.hasRemaining() && buffer.get() == SEPARATOR) {
                        print("FILE ENTRY [", lowerCaseFileName, "] AT OFFSET ["
                                    + (buffer.position() - fileName.limit()
                                                + startAtByteNum - 1), "]");
                        
                        buffer.mark();
                        byte[] foundHash = new byte[MHash.HASH_OUTPUT];
                        buffer.get(foundHash, 0, MHash.HASH_OUTPUT);
                        buffer.reset();
                        
                        print("EXE HASH [", MHash.toPrintString(foundHash), "]");
                        print("UPK HASH [", hash.toPrintString(), "]");
                        
                        // Write new hashes
                        if (!Arrays.equals(hash.getHash(), foundHash)) {
                            fc.position(startAtByteNum + buffer.position());
                            fc.write(ByteBuffer.wrap(hash.getHash()));
                            print("UPDATED [" + lowerCaseFileName, "] EXE HASH");
                        }
                        ++numFound;
                        if (sync != null) {
                            // check progress
                            sync.getSync().plusProgress(p);
                        }
                        break;
                    } else {
                        buffer.position(position++);
                    }
                    
                    if (sync != null) {
                        // check progress
                        if (buffer.position() % progressPlusRate == 0) {
                            sync.getSync().plusProgress(1);
                            p--;
                        }
                    }
                    if (numFound == numFiles) {
                        break;
                    }
                }
                buffer.rewind();
            }
            
        } finally {
            
            // Check and report errors
            if (numFound != numFiles) {
                return;
                
            } else if (numFound == 0) print("NO CHANGES NEED TO BE MADE.", "");
            else {
                print("[" + numFound, "] SHA HASH(ES) UPDATED.");
            }
        }
    }
    
    private static void print(String... strings) {
        
        print(MAIN, strings);
    }
}
