package org.xcom.main.shared;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.JFrame;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.xcom.main.shared.entities.Config;
import org.xcom.main.shared.entities.ModXml;
import org.xcom.mod.gui.streams.Stream;
import org.xcom.mod.gui.workers.RunInBackground;
import org.xcom.mod.tools.xshape.MHash;
import org.xcom.mod.tools.xshape.XShape;

public abstract class Main implements Runnable {
	
	public static final Charset DEFAULT_FILE_ENCODING = Charset.forName("UTF-8");
	
	public enum Error {
		
		NOTHING(""), //
		SYS_SERVICE_CREATE_FAIL("System services creation failed"), //
		CONFIG_FILE_GET_ERROR("Could not get/unmarshal/create config file."), //
		CONFIG_FILE_SAVE_ERROR("Could not marshal config file for saving."), //
		MOD_CONFIG_FILE_ERROR("Could not get/unmarshal/create mod config file."), //
		XSHAPE_CONFIG_ERROR("Could not verify XShape config file."), //
		XSHAPE_FILE_READ_ERROR("Could not read or get lines from XShape file."), //
		
		XCOM_PATH_ERROR("XCOM PATH VALIDATION ERROR"), //
		UNPACKED_PATH_ERROR("UNPACKED PATH INCORRECT"), //
		PROGRAM_ARGUMENTS("INCORRECT COMMAND LINE ARGUMENTS"), //
		MOD_UPK_FILE_ERROR("XMOD UPK FILE ERROR"), //
		MOD_EXPORT_FILE_ERROR("XMOD EXPORT FILE ERROR"), //
		XML_SAVE_ERROR("Xml failed to save."), //
		XML_PRINT_ERROR("Xml failed to print."), //
		INS_EXPORT_EXTRACTION("Export file extraction failed"), //
		INS_UPK_FILE_NF("Cooked Upk file not found."), //
		INS_UPK_FILE_NA("Cooked Upk file not accessible."), //
		INS_UPK_FILE_IO("Cooked Upk file use produced IO error."), //
		INS_UPK_FILE_COMPRESSED("Cooked Upk file still compressed."), //
		INS_UPK_RES_NF("Upk resource was not found in unpacked files"), //
		INS_FATAL("Installer stopped for unknown reason."), //
		
		MAK_MOD_ACCESS_ERROR("Could not access mod object or export file."), //
		MAK_MOD_IO_ERROR("Could not process changes."), //
		MAK_HASH_GET_ERROR("Could not calculate SHA hash."), //
		MAK_SAVE_MOD_FILES("Could not save mod files."), //
		MAK_UPK_FILE_NOTEXTRACTED("Upk file not extracted."), //
		
		XSHA_UPK_FILE_COMPRESSED("Cooked *.upk file still compressed."), //
		XSHA_HASH_GET_ERROR("Could not calculate SHA hash."), //
		XSHA_MOD_ACCESS_ERROR("Could not access file."), //
		XSHA_UPK_FILENAME_ERROR("Could not find upk filename. Please report this error."), //
		XSHA_PATCH_NOT_REQUIRED("Pacthing was not required."), //
		XSHA_INI_PATCHERROR("Error pathcing Ini file."),
		
		DEFAULT("THERE WAS AN ERROR"); //
		
		private final String msg;
		
		Error(String msg) {
			this.msg = String.format(("Error [%s] " + msg), this.ordinal());
		}
		public String getMsg() {
			return msg;
		}
	}
	
	protected Error ERROR;
	
	public final static String CONSOLE_SEPARATOR = "**************************"
				+ "*******************************" + "*******************************";
	public final static String SYSTEM_NAME = "xcmm";
	public final static String MAIN_DELEGATE = "";
	public final static String MAKE_DELEGATE = "";
	public final static String INSTALL_DELEGATE = "";
	public final static String ERROR_DELEGATE = "";
	
	protected final static String RELATIVE_EXE_PATH = "\\Binaries\\Win32\\XComGame.exe";
	protected final static String COMPRESSED_UPK_SIZE_EXT = ".uncompressed_size";
	protected final static String RELATIVE_TOC_PATH = "\\XComGame\\PCConsoleTOC.txt";
	private final static String UNPACKED_HEADER = "C1832A9E4D033B"; // C1 83 2A 9E
																																	// 4D 03 3B
	protected final static int NUM_CPU = (Runtime.getRuntime().availableProcessors());
	
	protected static Config config;
	protected static JFrame contentPane;
	protected static MessageDigest md;
	protected static JAXBContext jc;
	protected static Unmarshaller u;
	protected static Marshaller m;
	
	public static Stream MAIN = Stream.getStream(MAIN_DELEGATE);
	protected static Stream MAKE = Stream.getStream(MAKE_DELEGATE);
	public static Stream INSTALL = Stream.getStream(INSTALL_DELEGATE);
	public static Stream ERR = Stream.getErrorStream(ERROR_DELEGATE);
	
	public final static String USER_DIR = System.getProperty("user.dir");
	
	@SuppressWarnings("rawtypes")
	protected RunInBackground sync;
	protected Boolean DONE;
	protected static Boolean IN_GUI = false;
	protected volatile Object ret = null;
	
	protected static java.util.ArrayList<Path> editedUpks = new java.util.ArrayList<Path>();
	
	public Main() {
		DONE = false;
		ERROR = Error.NOTHING;
	}
	
	/**
	 * Checks if the XCom path is valid
	 * 
	 * @param config
	 * @return
	 */
	public static boolean isXComPathValid(String path) {
		
		return Files.exists(Paths.get(path, "Binaries", "Win32", "Version.txt"));
	}
	
	/**
	 * Checks if the XCom path is valid
	 * 
	 * @param config
	 * @return
	 */
	public static boolean isUnPackedPathValid(String path) {
		
		return Files
					.exists(Paths.get(path, "Core", "Component", "TemplateName.NameProperty"));
	}
	
	/**
	 * Appends complex msg to console. Add new line.
	 * 
	 * @param msg
	 */
	public static void print(Stream stream, String... strings) {
		
		String out = stream.getDelegate();
		for (String s : strings) {
			out += s;
		}
		print(stream, out + "\n");
	}
	
	/**
	 * Appends simple msg to console. No new line.
	 * 
	 * @param msg
	 */
	public static void print(String msg) {
		
		MAIN.append(msg);
	}
	
	/**
	 * Appends simple msg to console. No new line.
	 * 
	 * @param msg
	 */
	protected static void print(java.io.PrintStream ps, String msg) {
		
		ps.append(msg);
	}
	
	/**
	 * Tests if filenames in each list match each other.
	 * 
	 * Assumes both lists are the same size.
	 * 
	 * @param originalFiles
	 * @param editedFiles
	 * @return true if all matched
	 */
	public static boolean fileNamesMatch(List<Path> originalFiles, List<Path> editedFiles) {
		
		for (Path o : originalFiles) {
			boolean matched = false;
			for (Path e : editedFiles) {
				if (e.getFileName().equals(o.getFileName())) {
					matched = true;
					break;
				}
			}
			if (!matched) return false;
		}
		return true;
	}
	
	/**
	 * Save xml jaxb object into xml. Object must implement ModXml to get save
	 * path.
	 * 
	 * @param jaxBElement
	 * @param path
	 * @throws XmlSaveException
	 */
	public static void saveXml(ModXml jaxBElement) throws XmlSaveException {
		
		Path p = jaxBElement.getXmlSavePath();
		
		if (Files.notExists(p)) {
			try {
				Files.createDirectories(p.getParent());
			} catch (IOException e) {
				throw new XmlSaveException("IOException,Files.createDirectories");
			}
		}
		
		try (OutputStream os = Files.newOutputStream(p)) {
			m.marshal(jaxBElement, os);
			print("SAVED [" + p, "]");
		} catch (JAXBException e) {
			throw new XmlSaveException("JAXBException,Files.marshall");
		} catch (IOException e) {
			throw new XmlSaveException("IOException,Files.newOutputStream,close");
		}
	}
	
	/**
	 * Marshalls the config to xml and prints the xml to console.
	 * 
	 * @param config
	 */
	public static void printXml(ModXml xml, String msg) {
		
		try {
			printXml(MAIN, xml, "");
		} catch (XmlPrintException e) {
			print("NON FATAL ERROR: Could not print config file to stream.\n");
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Marshalls the config to xml and prints the xml to console.
	 * 
	 * @param config
	 */
	public static void printXml(ModXml xml) {
		
		try {
			printXml(MAIN, xml, "");
		} catch (XmlPrintException e) {
			print("NON FATAL ERROR: Could not print config file to stream.\n");
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Marshalls the config to xml and prints the xml to console.
	 * 
	 * @param config
	 * @throws XmlPrintException
	 */
	public static void printXml(java.io.PrintStream os, ModXml xml) {
		
		try {
			printXml(os, xml, "");
		} catch (XmlPrintException e) {
			print("NON FATAL ERROR: Could not print config file to stream.\n");
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Marshalls the config to xml and prints the xml to console.
	 * 
	 * @param config
	 * @throws XmlPrintException
	 */
	public static void printXml(java.io.PrintStream os, ModXml xml, String msg)
				throws XmlPrintException {
		
		print(os, CONSOLE_SEPARATOR + "\n" + xml.getPrintName()
					+ (msg != "" ? " " + msg : " XML\n"));
		try {
			m.marshal(xml, os);
		} catch (JAXBException e) {
			throw new XmlPrintException("JAXBException,marshall");
		} finally {
			print(CONSOLE_SEPARATOR + "\n");
		}
	}
	
	/**
	 * Copy one file to another file location.
	 * 
	 * @param from
	 * @param to
	 * @throws CopyFileException
	 */
	public static void copyFile(Path from, Path to, Boolean replaceExisting)
				throws CopyFileException {
		
		if (Files.notExists(from)) {
			throw new CopyFileException("FileNotFoundException,notExists");
		}
		if (!(Files.isDirectory(to.getParent()))) {
			try {
				Files.createDirectories(to.getParent());
			} catch (IOException e) {
				print("Could not create mod directories.", "");
				throw new CopyFileException("IOException,createDirectories");
			}
			print("CREATED [" + to.getParent(), "]");
		}
		
		try {
			// TODO may need to refine file saves when files exist
			if (!replaceExisting && Files.exists(to)) {
				return;
			} else if (replaceExisting) {
				Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
			} else {
				Files.copy(from, to);
			}
		} catch (IOException e) {
			throw new CopyFileException("IOException.copy");
		}
		print("COPIED [" + from.getFileName(), "] FROM [" + from.getParent(), "] TO ["
					+ to.getParent(), "]");
	}
	
	/**
	 * Returns whether a upk file is compressed or not.
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 */
	public static boolean isDecompressed(Path p) throws IOException {
		
		Boolean ret = true;
		try (InputStream fis = Files.newInputStream(p)) {
			final byte[] header = MHash.hexStringGetBytes(UNPACKED_HEADER);
			final byte[] barray = new byte[header.length];
			fis.read(barray);
			
			for (int i = barray.length - 1; i >= 0; --i) {
				if (barray[i] != header[i]) {
					ret = false;
				}
			}
		} catch (IOException e) {
			throw new IOException("decompressed,newINputStream,read");
		}
		return ret;
	}
	
	/**
	 * DO NOT USE THIS FOR HASH
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] getBytes(final String s) {
		return s.getBytes(Main.DEFAULT_FILE_ENCODING);
	}
	
	/**
	 * Uncompresses a file in a new process: this method WAITS.
	 * 
	 * @param p
	 */
	public static void decompress(Path p) {
		final String unpacked = Paths.get(XShape.getConfig().getUnpackedPath()).toString();
		final String fileToDecompress = p.toAbsolutePath().toString();
		final String fileName = p.getFileName().toString();
		
		final File log = new File("log");
		final String tool = Paths.get(config.getCompressorPath()).toAbsolutePath().toString();
		
		// The only way this works is with a SINGLE string
		ProcessBuilder pb = new ProcessBuilder("\"" + tool + "\" -lzo -out=\"" + unpacked
					+ "\" \"" + fileToDecompress + "\"");
		
		Process proc;
		
		try {
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.appendTo(log));
			proc = pb.start();
			
			assert pb.redirectInput() == Redirect.PIPE;
			assert pb.redirectOutput().file() == log;
			assert proc.getInputStream().read() == -1;
			
			@SuppressWarnings("unused")
			int rc = proc.waitFor();
			
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace(System.err);
		}
		print("DECOMPRESSED [" + fileName, "]");
	}
	
	/**
	 * Unpack upk fle - must be decompressed prior a file in a new process:
	 * however this method WAITS.
	 * 
	 * @param p
	 */
	public static Process extract(Path p) {
		
		final String unpacked = Paths.get(XShape.getConfig().getUnpackedPath()).toString();
		final String fileToUnpack = p.toAbsolutePath().toString();
		//final String fileName = p.getFileName().toString();
		
		final String tool = Paths.get(config.getExtractorPath()).toAbsolutePath().toString();
		
		//final File log = new File("log");		
		// The only way this works is with a SINGLE string
		ProcessBuilder pb = new ProcessBuilder("\"" + tool + "\" -lzo -out=\"" + unpacked
					+ "\" \"" + fileToUnpack + "\"");
		
		Process proc = null;
		
		try {
			pb.redirectErrorStream(true);
			// pb.redirectOutput(Redirect.appendTo(log));
			//pb.redirectOutput(Redirect.INHERIT);
			proc = pb.start();
			
			//assert pb.redirectInput() == Redirect.PIPE;
			//assert pb.redirectOutput().file() == log;
			//assert proc.getInputStream().read() == -1;
			
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
		}
		return proc;
	}
	
	/**
	 * After decompressing game files this will create backups and move redundant
	 * files.
	 * 
	 * @param p
	 * @throws IOException
	 */
	public static void sortGameFiles(Path p) throws IOException {
		
		final String unpacked = Paths.get(getConfig().getUnpackedPath()).toAbsolutePath()
					.toString();
		final String cooked = getConfig().getCookedPath().toAbsolutePath().toString();
		
		final String fileToDecompress = p.toAbsolutePath().toString();
		final String fileName = p.getFileName().toString();
		
		Path uncomp = Paths.get(unpacked, fileName);
		
		if (Files.exists(uncomp)) {
			try {
				print("SORTING FILE [" + fileName, "]");
				
				Path original = Paths.get(fileToDecompress);
				Path originalBackup = Paths.get(unpacked, fileName + ".original_compressed");
				
				Files.move(original, originalBackup, StandardCopyOption.REPLACE_EXISTING);
				print("MOVING [" + original, "] TO [" + originalBackup, "]");
				
				Files.copy(uncomp, original, StandardCopyOption.REPLACE_EXISTING);
				print("MOVING [" + uncomp, "] TO [" + original, "]");
				
				Path compSize = Paths.get(cooked, fileName + COMPRESSED_UPK_SIZE_EXT);
				Path compSizeBkp = Paths.get(unpacked, fileName + COMPRESSED_UPK_SIZE_EXT
							+ ".bkp");
				
				if (Files.exists(compSize)) {
					Files.move(compSize, compSizeBkp, StandardCopyOption.REPLACE_EXISTING);
					print("MOVING [" + compSize, "] TO [" + compSizeBkp + "]");
				}
			} catch (IOException ex) {
				throw new IOException();
			}
		}
	}
	
	/**
	 * Download a file and save it in 'temp' with filename saveAs
	 * 
	 * @param saveAs
	 * @param down
	 * 
	 * @return A path to the finished download.
	 * 
	 * @throws DownloadFailedException
	 */
	public static Path download(String saveAs, URL down) throws DownloadFailedException {
		Path temp = Paths.get("temp");
		try {
			if (Files.notExists(temp)) {
				Files.createDirectory(temp);
			}
			temp = Paths.get("temp", saveAs);
			if (Files.deleteIfExists(temp)) {
				Files.createFile(temp);
			}
		} catch (IOException ex) {
			throw new DownloadFailedException();
		}
		
		try (ReadableByteChannel rbc = Channels.newChannel(down.openStream());
					FileOutputStream fos = new FileOutputStream(temp.toString())) {
			
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
			
		} catch (IOException ex) {
			throw new DownloadFailedException();
		}
		print("DOWNLOADED [" + down.toString(), "]");
		return temp;
	}
	
	/**
	 * Unzip a fail.
	 * 
	 * @param zipDir
	 * @param saveToDir
	 * @throws ZipException
	 */
	public static void unZip(Path zipDir, Path saveToDir) throws ZipException {
		
		final int BUFFER = 8192;
		
		ZipFile zip = null;
		ZipEntry entry;
		
		try {
			zip = new ZipFile(zipDir.toFile());
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
		}
		
		@SuppressWarnings("unchecked")
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
		
		while (entries.hasMoreElements()) {
			entry = (ZipEntry) entries.nextElement();
			File f = new java.io.File(saveToDir.toFile(), entry.getName());
			
			if (entry.isDirectory()) {
				continue;
			}
			if (!f.exists()) {
				f.getParentFile().mkdirs();
				try {
					f.createNewFile();
				} catch (IOException ex) {
					ex.printStackTrace(System.err);
					throw new ZipException();
				}
			}
			try (InputStream is = zip.getInputStream(entry);
						OutputStream os = new FileOutputStream(f)) {
				
				int r;
				byte buffer[] = new byte[BUFFER];
				while ((r = is.read(buffer)) != -1) {
					os.write(buffer, 0, r);
				}
				
			} catch (IOException ex) {
				ex.printStackTrace(System.err);
				throw new ZipException();
			}
			print("UNZIPPED [" + zipDir.toString(), "]");
			
		}
	}
	
	/**
	 * Opens the desktop browser to url string
	 * 
	 * @param url
	 */
	public static void openDesktopBrowser(String url) throws MalformedURLException {
		if (Desktop.isDesktopSupported()) {
			
			try {
				Desktop.getDesktop().browse(new URL(url).toURI());
			} catch (IOException | URISyntaxException ex) {
				ex.printStackTrace(System.err);
			}
		}
	}
	
	/**
	 * Prints the XCM action start console message
	 */
	protected static void printActionMessage(String owner) {
		
		print(owner, " ACTION");
	}
	
	public static Config getConfig() {
		return config;
	}
	
	protected static MessageDigest getMd() {
		return md;
	}
	
	protected static JAXBContext getJc() {
		return jc;
	}
	
	protected static Unmarshaller getU() {
		return u;
	}
	
	protected static Marshaller getM() {
		return m;
	}
	
	private static void print(String... strings) {
		print(MAIN, strings);
	}
	
	public Boolean getDone() {
		
		return DONE;
	}
	
	public void setDone(Boolean done) {
		this.DONE = done;
	}
	
	public synchronized Object getRet() {
		return this.ret;
	}
	
	public Error getError() {
		return ERROR;
	}
	
	public void setError(Error error) {
		this.ERROR = error;
	}
	
	public abstract void run();
	
	public static JFrame getFrame() {
		return contentPane;
	}
	
}
