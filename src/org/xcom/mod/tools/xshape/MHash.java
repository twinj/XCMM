package org.xcom.mod.tools.xshape;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.xcom.mod.tools.xshape.exceptions.CalculateHashException;

final public class MHash {

	private static final int DEFAULT_BUFFER = 8096;
	public static final String ALGORITHM = "SHA";
	public static final int HASH_OUTPUT = 20;
	public static final String TEMP = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

	private Path path;
	private byte[] fileNameAsBytes;
	private byte[] hash;
	private static MessageDigest md;

	/**
	 * Creates a normal data construct for a MHash
	 * 
	 * @param path
	 * @param hash
	 * @throws CalculateHashException 
	 */
	public MHash(Path path, byte[] hash) throws CalculateHashException {
		this(path);
		this.hash = hash;
	}

	/**
	 * Generates hash information and hash.
	 * 
	 * @param path
	 * @throws CalculateHashException 
	 */
	public MHash(final Path path) throws CalculateHashException {
		super();
		try {
			md = MessageDigest.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException ignore) {
			// Should never happen it is fixed
			ignore.printStackTrace(System.err);
		}
		this.path = path;
		this.fileNameAsBytes = getPathFileName().toLowerCase().getBytes();
		hash = calculateHash(path);
	}

	/**
	 * Will calculate hashes for a list of paths.
	 *
	 * @throws CalculateHashException 
	 * 
	 */
	public static List<MHash> calculateHashes(List<Path> paths) throws CalculateHashException {

		List<MHash> hashes = new ArrayList<MHash>();

		for (Path path : paths) {
			hashes.add(new MHash(path));
		}
		return hashes;
	}

	/**
	 * Calculates an SHA hash for the input Path.
	 * 
	 * @param path
	 * @return a byte array of size 20.. will only work for SHA output.
	 * @throws CalculateHashException 
	 */
	public static byte[] calculateHash(Path path) throws CalculateHashException {

		byte[] dataBytes = new byte[DEFAULT_BUFFER];
		byte[] hash = new byte[HASH_OUTPUT];
		int numRead = 0;

		try (InputStream is = Files.newInputStream(path)) {
			while ((numRead = is.read(dataBytes)) > 0) {
				md.update(dataBytes, 0, numRead);
			}
			hash = md.digest();
		} catch (IOException e) {
			throw new CalculateHashException("IOException,newInputStream");
		} finally {
			if (md != null) {
				md.reset();
			}			
		}
		return hash;
	}

	/**
	 * Get hash of file at file path
	 * 
	 * @param path
	 * 
	 * @return hash string
	 * @throws CalculateHashException 
	 */
	public static String getHashString(Path path) throws CalculateHashException {
		return new MHash(path).toString();
	}
	
	/**
	 * Get hash of file at file path
	 * 
	 * @param path
	 * 
	 * @return hash string
	 * @throws CalculateHashException 
	 */
	public static String getHashPrintString(Path path) throws CalculateHashException {
		return new MHash(path).toPrintString();
	}

	/**
	 * Returns a hash byte array as a hex string
	 * 
	 * @param hash
	 * 
	 * @return String
	 */
	public static String toString(byte[] hash) {
		StringBuilder result = new StringBuilder(HASH_OUTPUT*2);
		for (byte b : hash) {
			String hexString = Integer.toHexString((int) b & 0xff)
					.toUpperCase();
			if (hexString.length() == 1)
				result.append("0");
			result.append(hexString);
		}
		return result.toString();
	}
	
	/**
	 * Returns a hash byte array as a hex string with spaces.
	 * 
	 * @param hash
	 * 
	 * @return String
	 */
	public static String toPrintString(byte[] hash) {
		StringBuilder result = new StringBuilder(HASH_OUTPUT*3);
		for (byte b : hash) {
			String hexString = Integer.toHexString((int) b & 0xff)
					.toUpperCase();
			if (hexString.length() == 1)
				result.append("0");
			result.append(hexString).append(" ");
		}
		return result.toString();
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] hexStringToBytes(final String s) {
		return DatatypeConverter.parseHexBinary(s);
	}

	/**
	 * Returns a hash byte array as a hex string
	 * 
	 * @param hash
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		return toString(hash);
	}
	
	/**
	 * Returns a hash byte array as a hex string with spaces.
	 * 
	 * @param hash
	 * 
	 * @return String
	 */
	public String toPrintString() {
		return toPrintString(hash);
	}

	public Path getPath() {
		return path;
	}

	public byte[] getHash() {
		return hash;
	}

	public String getHashString() {
		return hash.toString();
	}

	public byte[] getFileNameBytes() {
		return fileNameAsBytes;
	}

	public String getPathFileName() {
		return path.getFileName().toString();
	}
}