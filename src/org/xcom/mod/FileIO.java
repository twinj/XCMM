package org.xcom.mod;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Eric Bruno
 */
public class FileIO {
	String myXML = "<Configuration>\n" + "   <Server>\n"
			+ "       <Name>MyServer</Name>\n"
			+ "       <Address>192.168.0.1</Address>\n" + "   </Server>\n"
			+ "   <RetryCount>10</RetryCount>\n" + "   <Logs>\n"
			+ "       <FilePath>/logs/out.txt</FilePath>\n"
			+ "       <Level>INFO</Level>\n" + "   </Logs>\n"
			+ "</Configuration>";

	public static void main(String[] args) {
		FileIO io = new FileIO();
		List<String> lines = null;

		// Write XML String bytes to a file
		io.writeFileBytes("config.xml", io.myXML);

		// Read all bytes from small file at once
		System.out.println("\n-- TEST 1 ---------------------------");
		String config = new String(io.readSmallFileBytes("config.xml"));
		System.out.println(config);

		// Read all lines from a small file at once
		System.out.println("\n-- TEST 2 ---------------------------");
		lines = io.readSmallFileLines("config.xml");
		for (String line : lines)
			System.out.println(line);

		// Read all lines for a larger file
		System.out.println("\n-- TEST 3 ---------------------------");
		lines = io.readLargeFileLines("log.txt");
		for (String line : lines)
			System.out.println(line);

		// Read all lines for a larger file
		System.out.println("\n-- TEST 4 ---------------------------");
		lines = io.readLargeFileLinesMixed("log.txt");
		for (String line : lines)
			System.out.println(line);
	}

	public FileIO() {
	}

	public byte[] readSmallFileBytes(String name) {
		byte[] filearray = null;
		try {
			Path path = FileSystems.getDefault().getPath(".", name);
			return Files.readAllBytes(path);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return filearray;
	}

	public List<String> readSmallFileLines(String name) {
		try {
			return Files.readAllLines(
					FileSystems.getDefault().getPath(".", name),
					Charset.defaultCharset());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}

	public List<String> readLargeFileLines(String name) {
        try {
            BufferedReader reader = 
                Files.newBufferedReader(
                    FileSystems.getDefault().getPath(".", name), 
                    Charset.defaultCharset() );
 
            List<String> lines = new ArrayList<String>();
            String line = null;
            while ( (line = reader.readLine()) != null )
                lines.add(line);
 
            return lines;
        } 
        catch (IOException ioe) {
            ioe.printStackTrace();
        }        
        return null;
    }

	public List<String> readLargeFileLinesMixed(String name) {
        try {
            Path path = FileSystems.getDefault().getPath(".", name);
            InputStream in = Files.newInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
             
            List<String> lines = new ArrayList<String>();
            String line = null;
            while ( (line = reader.readLine()) != null )
                lines.add(line);
 
            return lines;
        }
        catch ( IOException ioe ) {
            ioe.printStackTrace();
        }
         
        return null;
    }

	void writeFileBytes(String filename, String content) {
		try {
			Files.write(FileSystems.getDefault().getPath(".", filename),
					content.getBytes(), StandardOpenOption.CREATE);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	void writeFileBytesBuffered(String filename, String content) {
		try {
			BufferedWriter writer = Files.newBufferedWriter(FileSystems
					.getDefault().getPath(".", filename), Charset
					.forName("US-ASCII"), StandardOpenOption.CREATE);

			writer.write(content, 0, content.length());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}