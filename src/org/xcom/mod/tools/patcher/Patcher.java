package org.xcom.mod.tools.patcher;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.xcom.mod.Main;
import org.xcom.mod.tools.maker.exceptions.ProcessFileChangesException;

public class Patcher extends Main {
	
	private Path toPatch;
	private File newR;
	
	public Patcher(Path toPatch, File newR) {
		super();
		this.toPatch = toPatch;
		this.newR = newR;
	}
	
	public void runc() {
		
	}
	
	@Override
	public void run() {
		printActionMessage("PATCHER");
		print("CREATING A SESSION BACKUP");
		
		try {
			Files.copy(toPatch, Paths.get("temp", toPatch.getFileName().toString()));
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
		byte[] bytes = null;
    try {
        bytes = Files.readAllBytes(newR.toPath());
    } catch (IOException ex) {
   // TODO Auto-generated catch block
   			ex.printStackTrace();
    }
		
		try (FileChannel fc = FileChannel.open(toPatch, StandardOpenOption.READ,
				StandardOpenOption.WRITE); FileLock lock = fc.tryLock()) {
			
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
	
}
